package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.dto.ShowtimeRequestDto;
import com.att.tdp.popcorn_palace.dto.ShowtimeResponseDto;
import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.model.Ticket;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TicketRepository;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    private ShowtimeRequestDto createValidRequest() {
        return ShowtimeRequestDto.builder()
                .movieId(1L)
                .theater("A1")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusHours(3))
                .price(50.0f)
                .build();
    }

    private Movie createMovie() {
        return Movie.builder().id(1L).duration(120).build();
    }

    @Test
    void addShowtime_WithValidInput_ShouldReturnShowtimeResponse() {
        ShowtimeRequestDto request = createValidRequest();
        Movie movie = createMovie();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(showtimeRepository.save(any())).thenAnswer(i -> {
            Showtime s = i.getArgument(0);
            s.setId(10L);
            return s;
        });

        ShowtimeResponseDto response = showtimeService.addShowtime(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        verify(showtimeRepository).save(any());
    }

    @Test
    void addShowtime_WithShorterDurationThanMovie_ShouldThrowException() {
        ShowtimeRequestDto request = ShowtimeRequestDto.builder()
                .movieId(1L)
                .theater("A1")
                .startTime(LocalDateTime.now().plusHours(1))
                .endTime(LocalDateTime.now().plusMinutes(30))
                .price(50.0f)
                .build();
        Movie movie = createMovie();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        AppException ex = assertThrows(AppException.class, () -> showtimeService.addShowtime(request));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.INVALID_SHOWTIME);
    }

    @Test
    void addShowtime_WhenOverlapping_ShouldThrowException() {
        ShowtimeRequestDto request = createValidRequest();
        Movie movie = createMovie();

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(new Showtime())));

        AppException ex = assertThrows(AppException.class, () -> showtimeService.addShowtime(request));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.OVERLAPPING_SHOWTIME);
    }

    @Test
    void updateShowtime_WithValidData_ShouldUpdateSuccessfully() {
        ShowtimeRequestDto request = createValidRequest();
        Showtime existing = Showtime.builder().id(1L).build();
        Movie movie = createMovie();

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any())).thenReturn(new ArrayList<>());
        when(showtimeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ShowtimeResponseDto response = showtimeService.updateShowtime(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getTheater()).isEqualTo("A1");
    }

    @Test
    void updateShowtime_WhenMovieNotFound_ShouldThrow() {
        ShowtimeRequestDto request = createValidRequest();
        Showtime existing = Showtime.builder().id(1L).build();

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> showtimeService.updateShowtime(1L, request));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.MOVIE_NOT_FOUND);
    }

    @Test
    void updateShowtime_WhenOverlapping_ShouldThrow() {
        ShowtimeRequestDto request = createValidRequest();
        Showtime existing = Showtime.builder().id(1L).build();
        Movie movie = createMovie();

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any())).thenReturn(new ArrayList<>(List.of(new Showtime())));

        AppException ex = assertThrows(AppException.class, () -> showtimeService.updateShowtime(1L, request));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.OVERLAPPING_SHOWTIME);
    }

    @Test
    void updateShowtime_WhenShowtimeNotFound_ShouldThrow() {
        ShowtimeRequestDto request = createValidRequest();
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> showtimeService.updateShowtime(1L, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteShowtime_ShouldDeleteOnlyItsTickets() {
        Ticket t1 = Ticket.builder().id(1L).build();
        Ticket t2 = Ticket.builder().id(2L).build();
        Showtime showtime = Showtime.builder().id(1L).tickets(Set.of(t1, t2)).build();

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        showtimeService.deleteShowtime(1L);

        verify(ticketRepository).delete(t1);
        verify(ticketRepository).delete(t2);
        verify(showtimeRepository).deleteById(1L);
    }

    @Test
    void deleteShowtime_WhenNotFound_ShouldThrowException() {
        assertThatThrownBy(() -> showtimeService.deleteShowtime(999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getShowtimeById_WhenNotFound_ShouldThrowException() {
        when(showtimeRepository.findById(404L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> showtimeService.getShowtimeById(404L));
        assertThat(ex.getErrorType()).isEqualTo(ErrorType.SHOWTIME_NOT_FOUND);
    }
}
