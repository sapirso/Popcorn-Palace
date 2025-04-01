package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.dto.MovieRequestDto;
import com.att.tdp.popcorn_palace.dto.MovieResponseDto;
import com.att.tdp.popcorn_palace.dto.MovieUpdateRequestDto;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.model.Ticket;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;

    public MovieService(MovieRepository movieRepository, ShowtimeRepository showtimeRepository, TicketRepository ticketRepository) {
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<MovieResponseDto> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MovieResponseDto addMovie(MovieRequestDto movieDto) {

        // Check for existing movie with the same title to avoid duplicates
        final boolean titleExists = movieRepository.findAll().stream()
                .anyMatch(m -> m.getTitle().equalsIgnoreCase(movieDto.getTitle()));

        // If the movie title already exists, throw a conflict exception
        if (titleExists) {
            throw new AppException(
                    "A movie titled '" + movieDto.getTitle() + "' already exists in the system",
                    HttpStatus.CONFLICT,
                    ErrorType.DUPLICATE_MOVIE_TITLE
            );
        }

        final Movie movie = Movie.builder()
                .title(movieDto.getTitle())
                .genre(movieDto.getGenre())
                .duration(movieDto.getDuration())
                .rating(movieDto.getRating())
                .releaseYear(movieDto.getReleaseYear())
                .build();

        final Movie saved = movieRepository.save(movie);

        return convertToResponseDto(saved);
    }

    @Transactional
    public MovieResponseDto updateMovieByTitle(String title,  MovieUpdateRequestDto updatedDto) {

        // Verify movie existence, throw an exception if the movie is not found
        final Movie existing = movieRepository.findByTitle(title)
                .orElseThrow(() -> new AppException(
                        "Movie with title '" + title + "' not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.MOVIE_NOT_FOUND
                ));

        // Update title if provided, with duplicate title check
        if (updatedDto.getTitle() != null && !updatedDto.getTitle().equalsIgnoreCase(existing.getTitle())) {
            if (movieRepository.findAll().stream()
                    .anyMatch(movie -> movie.getTitle().equalsIgnoreCase(updatedDto.getTitle()))) {
                throw new AppException(
                        "A movie titled '" + updatedDto.getTitle() + "' already exists in the system",
                        HttpStatus.CONFLICT,
                        ErrorType.DUPLICATE_MOVIE_TITLE
                );
            }
            existing.setTitle(updatedDto.getTitle());
        }

        // Validate and update movie details, ensuring data integrity and preventing invalid modifications
        if (updatedDto.getGenre() != null) {
            if (!updatedDto.getGenre().isEmpty())
                existing.setGenre(updatedDto.getGenre());
        }

        if (updatedDto.getDuration() != null) {
            if (updatedDto.getDuration() < 0) {
                throw new AppException("Duration cannot be negative", HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR);
            }

            existing.setDuration(updatedDto.getDuration());
        }

        if (updatedDto.getRating() != null) {
            if (updatedDto.getRating() < 0 || updatedDto.getRating() > 10) {
                throw new AppException("Rating must be between 0 and 10", HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR);
            }

            existing.setRating(updatedDto.getRating());
        }

        if (updatedDto.getReleaseYear() != null) {
            if (updatedDto.getReleaseYear() < 0) {
                throw new AppException("ReleaseYear cannot be negative", HttpStatus.BAD_REQUEST, ErrorType.VALIDATION_ERROR);
            }
            existing.setReleaseYear(updatedDto.getReleaseYear());
        }

        final Movie updated = movieRepository.save(existing);
        return convertToResponseDto(updated);
    }

    @Transactional
    public void deleteMovieByTitle(String title) {
        final Movie movie = movieRepository.findByTitle(title)
                .orElseThrow(() -> new AppException(
                        "Movie not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.MOVIE_NOT_FOUND,
                        "Movie with title '" + title + "' does not exist"
                ));
        try {
            for (Showtime showtime : movie.getShowtimes()) {
                for (Ticket ticket : showtime.getTickets()) {
                    ticketRepository.delete(ticket);
                }
                showtimeRepository.deleteById(showtime.getId());
            }
        }
        catch (Exception e) {
            throw new AppException(
                    "Internal Data base error",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorType.INTERNAL_SERVER_ERROR,
                    "Error:" + e + "\nBacktrace:" + e.getStackTrace()
            );

        }

        try {
            movieRepository.deleteById(movie.getId());
                    } catch (Exception e) {
            throw new AppException(
                    "Internal Data base error",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorType.INTERNAL_SERVER_ERROR,
                    "Error:" + e
            );
        }
    }

    // Convert Movie model to MovieResponseDto for API response
    private MovieResponseDto convertToResponseDto(Movie movie) {
        return MovieResponseDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .genre(movie.getGenre())
                .duration(movie.getDuration())
                .rating(movie.getRating())
                .releaseYear(movie.getReleaseYear())
                .build();
    }
}




