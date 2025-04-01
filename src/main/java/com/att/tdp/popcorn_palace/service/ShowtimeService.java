package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.dto.ShowtimeResponseDto;
import com.att.tdp.popcorn_palace.dto.ShowtimeRequestDto;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.model.Ticket;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final TicketRepository ticketRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository,
                           MovieRepository movieRepository,
                           TicketRepository ticketRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public ShowtimeResponseDto addShowtime(ShowtimeRequestDto requestDto) {

        // Verify movie existence, throw an exception if the movie is not found
        final Movie movie = movieRepository.findById(requestDto.getMovieId())
                .orElseThrow(() -> new AppException(
                        "Movie with ID '" + requestDto.getMovieId() + "' not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.MOVIE_NOT_FOUND
                ));

        //Validate showtime data
        validateShowtime(requestDto, movie);

        // Ensure no scheduling conflicts in the same theater
        checkForOverlappingShowtimes(null, requestDto.getTheater(),
                requestDto.getStartTime(), requestDto.getEndTime());

        // Create new showtime entity
        final Showtime showtime = Showtime.builder()
                .movieId(requestDto.getMovieId())
                .theater(requestDto.getTheater())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .price(requestDto.getPrice())
                .build();

        final Showtime saved = showtimeRepository.save(showtime);
        return convertToResponseDTO(saved);
    }


    @Transactional(readOnly = true)
    public ShowtimeResponseDto getShowtimeById(Long id) {

        // Verify showtime existence, throw an exception if the showtime is not found
        final Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Showtime with ID '" + id + "' not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.SHOWTIME_NOT_FOUND
                ));

        // Convert and return showtime as a response DTO
        return convertToResponseDTO(showtime);
    }


    @Transactional
    public ShowtimeResponseDto updateShowtime(Long id, ShowtimeRequestDto requestDto) {

        // Verify showtime existence, throw an exception if the showtime is not found
        final Showtime existing = showtimeRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Showtime with ID '" + id + "' not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.SHOWTIME_NOT_FOUND
                ));

        // Verify movie ID existence, throw an exception if the movie ID is not found
        final Movie movie = movieRepository.findById(requestDto.getMovieId())
                .orElseThrow(() -> new AppException(
                        "Movie with ID '" + requestDto.getMovieId() + "' not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.MOVIE_NOT_FOUND
                ));

        // Validate showtime data
        validateShowtime(requestDto, movie);

        // Ensure no scheduling conflicts in the same theater
        checkForOverlappingShowtimes(
                id,
                requestDto.getTheater(),
                requestDto.getStartTime(),
                requestDto.getEndTime()
        );

        existing.setMovieId(requestDto.getMovieId());
        existing.setTheater(requestDto.getTheater());
        existing.setStartTime(requestDto.getStartTime());
        existing.setEndTime(requestDto.getEndTime());
        existing.setPrice(requestDto.getPrice());

        final Showtime updated = showtimeRepository.save(existing);

        // Convert and return showtime as a response DTO
        return convertToResponseDTO(updated);
    }


    @Transactional
    public void deleteShowtime(Long id) {

        // Verify showtime existence, throw an exception if the showtime is not found
        final Showtime showtime = showtimeRepository.findById(id).orElseThrow(() -> new AppException(
                "showtime not found",
                HttpStatus.NOT_FOUND,
                ErrorType.SHOWTIME_NOT_FOUND,
                "Movie with title '" + id + "' does not exist"
        ));

        for (Ticket ticket : showtime.getTickets()) {
            ticketRepository.delete(ticket);
        }

        showtimeRepository.deleteById(id);
    }

    // Validate showtime duration against movie length, ensuring sufficient screening time
    private void validateShowtime(ShowtimeRequestDto requestDto, Movie movie) {
        final Duration duration = Duration.between(requestDto.getStartTime(), requestDto.getEndTime());
        final int movieDuration = movie.getDuration();
        final long showtimeDuration = duration.toMinutes();

        if (showtimeDuration < movieDuration) {
            throw new AppException(
                    "Showtime duration is shorter than the movie duration",
                    HttpStatus.BAD_REQUEST,
                    ErrorType.INVALID_SHOWTIME,
                    "Showtime duration (" + showtimeDuration + " minutes) is shorter than the movie duration (" + movieDuration + " minutes)"
            );
        }

    }

    // Check for scheduling conflicts by identifying overlapping showtimes in the same theater
    private void checkForOverlappingShowtimes(Long currentId, String theater,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        final List<Showtime> overlappingShowtimes = showtimeRepository.findOverlappingShowtimes(
                theater, startTime, endTime);

        overlappingShowtimes.removeIf(s ->
                s.getId() != null && currentId != null && s.getId().equals(currentId));

        if (!overlappingShowtimes.isEmpty()) {
            throw new AppException(
                    "There is already a showtime scheduled in theater '" + theater +
                            "' that overlaps with the specified time period.",
                    HttpStatus.CONFLICT,
                    ErrorType.OVERLAPPING_SHOWTIME,
                    "Conflicting showtimes found in theater '" + theater + "' between " +
                            startTime + " and " + endTime
            );
        }
    }

    private Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Movie with ID '" + id + "' not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.MOVIE_NOT_FOUND
                ));
    }

    // Convert Movie model to MovieResponseDto for API response
    private ShowtimeResponseDto convertToResponseDTO(Showtime showtime) {
        return ShowtimeResponseDto.builder()
                .id(showtime.getId())
                .theater(showtime.getTheater())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .price(showtime.getPrice())
                .movieTitle(getMovieById(showtime.getMovieId()).getTitle())
                .movieReleaseYear(getMovieById(showtime.getMovieId()).getReleaseYear())
                .build();
    }


}









