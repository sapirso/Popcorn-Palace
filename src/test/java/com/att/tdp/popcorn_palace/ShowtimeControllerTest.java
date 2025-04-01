package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.dto.ShowtimeRequestDto;
import com.att.tdp.popcorn_palace.dto.ShowtimeResponseDto;
import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.Showtime;
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
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeControllerTests {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    @Test
    void addShowtime_WithValidData_ShouldSaveAndReturnResponse() {
        ShowtimeRequestDto dto = ShowtimeRequestDto.builder()
                .movieId(1L)
                .theater("Hall A")
                .startTime(LocalDateTime.of(2025, 4, 1, 12, 0))
                .endTime(LocalDateTime.of(2025, 4, 1, 14, 0))
                .price(40.0f)
                .build();

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setDuration(100);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any())).thenReturn(Collections.emptyList());
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(invocation -> {
            Showtime saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        ShowtimeResponseDto result = showtimeService.addShowtime(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getTheater()).isEqualTo("Hall A");

        verify(showtimeRepository).save(any(Showtime.class));
    }

    @Test
    void addShowtime_WithShorterDurationThanMovie_ShouldThrowAppException() {
        ShowtimeRequestDto dto = ShowtimeRequestDto.builder()
                .movieId(1L)
                .theater("Hall A")
                .startTime(LocalDateTime.of(2025, 4, 1, 12, 0))
                .endTime(LocalDateTime.of(2025, 4, 1, 12, 30))
                .price(40.0f)
                .build();

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setDuration(100);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        AppException exception = assertThrows(AppException.class, () -> showtimeService.addShowtime(dto));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.INVALID_SHOWTIME);
        assertThat(exception.getStatus().value()).isEqualTo(400);
    }

    @Test
    void updateShowtime_WithValidData_ShouldUpdateAndReturnResponse() {
        ShowtimeRequestDto dto = ShowtimeRequestDto.builder()
                .movieId(1L)
                .theater("Hall B")
                .startTime(LocalDateTime.of(2025, 4, 1, 16, 0))
                .endTime(LocalDateTime.of(2025, 4, 1, 18, 0))
                .price(50.0f)
                .build();

        Showtime existing = new Showtime();
        existing.setId(1L);

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setDuration(90);

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showtimeRepository.findOverlappingShowtimes(any(), any(), any())).thenReturn(Collections.emptyList());
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShowtimeResponseDto result = showtimeService.updateShowtime(1L, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTheater()).isEqualTo("Hall B");
    }

    @Test
    void updateShowtime_WhenShowtimeNotFound_ShouldThrowAppException() {
        ShowtimeRequestDto dto = ShowtimeRequestDto.builder()
                .movieId(1L)
                .build();

        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> showtimeService.updateShowtime(1L, dto));
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.SHOWTIME_NOT_FOUND);
    }


    @Test
    void deleteShowtime_WhenExists_ShouldDelete() {
        Showtime showtime = Showtime.builder()
                .id(1L)
                .tickets(Set.of())
                .build();

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));

        showtimeService.deleteShowtime(1L);

        verify(showtimeRepository).deleteById(1L);
    }


    @Test
    void deleteShowtime_WhenNotFound_ShouldThrowAppException() {

        AppException exception = assertThrows(AppException.class, () -> showtimeService.deleteShowtime(1L));
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.SHOWTIME_NOT_FOUND);
    }

    @Test
    void getShowtimeById_WhenExists_ShouldReturnShowtimeResponseDto() {
        Showtime showtime = new Showtime();
        showtime.setId(100L);
        showtime.setTheater("Main Hall");
        showtime.setStartTime(LocalDateTime.of(2025, 4, 1, 10, 0));
        showtime.setEndTime(LocalDateTime.of(2025, 4, 1, 12, 0));
        showtime.setPrice(45.0f);

        Movie movie = new Movie();
        movie.setId(100L);
        movie.setTitle("Sample Movie");
        movie.setReleaseYear(2024);

        showtime.setMovieId(movie.getId());

        when(showtimeRepository.findById(100L)).thenReturn(Optional.of(showtime));
        when(movieRepository.findById(100L)).thenReturn(Optional.of(movie));

        ShowtimeResponseDto response = showtimeService.getShowtimeById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTheater()).isEqualTo("Main Hall");
        assertThat(response.getMovieTitle()).isEqualTo("Sample Movie");
        assertThat(response.getMovieReleaseYear()).isEqualTo(2024);
    }
}
