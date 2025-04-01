package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.dto.MovieRequestDto;
import com.att.tdp.popcorn_palace.dto.MovieResponseDto;
import com.att.tdp.popcorn_palace.dto.MovieUpdateRequestDto;
import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.model.Ticket;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TicketRepository;
import com.att.tdp.popcorn_palace.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private Showtime showtime;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        movie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .genre("Action")
                .duration(120)
                .rating(8.5)
                .releaseYear(2022)
                .build();

        showtime = Showtime.builder()
                .id(10L)
                .movieId(movie.getId())
                .build();

        ticket = Ticket.builder()
                .id(100L)
                .showtimeId(showtime.getId())
                .build();

        movie.setShowtimes(Set.of(showtime));
        showtime.setTickets(Set.of(ticket));
    }

    private MovieRequestDto createValidMovieRequest() {
        return MovieRequestDto.builder()
                .title("Valid Movie")
                .genre("Action")
                .duration(120)
                .rating(8.0)
                .releaseYear(2023)
                .build();
    }

    private MovieUpdateRequestDto createPartialUpdateRequest() {
        return MovieUpdateRequestDto.builder()
                .genre("Updated Genre")
                .rating(9.0)
                .build();
    }

    private MovieUpdateRequestDto createInvalidUpdateRequest() {
        return MovieUpdateRequestDto.builder()
                .duration(-30)
                .genre("")
                .build();
    }

    @Test
    void addMovie_WithValidInput_ShouldReturnSavedMovie() {
        MovieRequestDto request = createValidMovieRequest();

        when(movieRepository.findAll()).thenReturn(Collections.emptyList());

        Movie saved = Movie.builder()
                .id(1L)
                .title(request.getTitle())
                .genre(request.getGenre())
                .duration(request.getDuration())
                .rating(request.getRating())
                .releaseYear(request.getReleaseYear())
                .build();

        when(movieRepository.save(any(Movie.class))).thenReturn(saved);

        MovieResponseDto response = movieService.addMovie(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Valid Movie");
        assertThat(response.getId()).isEqualTo(1L);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void addMovie_WithDuplicateTitle_ShouldThrowAppException() {
        MovieRequestDto request = createValidMovieRequest();

        Movie existingMovie = Movie.builder()
                .id(1L)
                .title(request.getTitle())
                .genre("Action")
                .duration(120)
                .rating(8.0)
                .releaseYear(2022)
                .build();

        when(movieRepository.findAll()).thenReturn(List.of(existingMovie));

        AppException ex = assertThrows(AppException.class, () -> movieService.addMovie(request));

        assertThat(ex.getErrorType()).isEqualTo(ErrorType.DUPLICATE_MOVIE_TITLE);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void getAllMovies_ShouldReturnListOfMovies() {
        when(movieRepository.findAll()).thenReturn(List.of(movie));

        List<MovieResponseDto> result = movieService.getAllMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
    }

    @Test
    void deleteMovieByTitle_WhenExists_ShouldDeleteMovieAndCascade() {
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));

        movieService.deleteMovieByTitle("Test Movie");

        verify(movieRepository).deleteById(1L);
    }

    @Test
    void deleteMovieByTitle_WhenNotFound_ShouldThrowException() {
        when(movieRepository.findByTitle("Missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.deleteMovieByTitle("Missing"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteMovieByTitle_WhenNoShowtimes_ShouldStillDelete() {
        movie.setShowtimes(Collections.emptySet());
        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));

        movieService.deleteMovieByTitle("Test Movie");

        verify(movieRepository).deleteById(1L);
    }

    @Test
    void deleteMovieByTitle_ShouldDeleteRelatedShowtimesAndTickets() {
        Ticket ticket1 = Ticket.builder().id(1L).build();
        Ticket ticket2 = Ticket.builder().id(2L).build();

        Showtime showtime1 = Showtime.builder()
                .id(10L)
                .tickets(Set.of(ticket1, ticket2))
                .build();

        Movie testMovie = Movie.builder()
                .id(1L)
                .title("Test Movie")
                .genre("Action")
                .duration(120)
                .rating(8.0)
                .releaseYear(2023)
                .showtimes(Set.of(showtime1))
                .build();

        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(testMovie));

        movieService.deleteMovieByTitle("Test Movie");

        verify(ticketRepository).delete(ticket1);
        verify(ticketRepository).delete(ticket2);
        verify(showtimeRepository).deleteById(showtime1.getId());
        verify(movieRepository).deleteById(1L);
    }

    @Test
    void updateMovie_WithPartialFields_ShouldUpdateOnlyProvidedFields() {
        MovieUpdateRequestDto request = createPartialUpdateRequest();

        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        MovieResponseDto result = movieService.updateMovieByTitle("Test Movie", request);

        assertThat(result.getGenre()).isEqualTo("Updated Genre");
        assertThat(result.getTitle()).isEqualTo("Test Movie");
    }

    @Test
    void updateMovie_WithInvalidFields_ShouldThrowAppException() {
        MovieUpdateRequestDto request = createInvalidUpdateRequest();

        when(movieRepository.findByTitle("Test Movie")).thenReturn(Optional.of(movie));

        assertThatThrownBy(() -> movieService.updateMovieByTitle("Test Movie", request))
                .isInstanceOf(AppException.class);
    }

}
