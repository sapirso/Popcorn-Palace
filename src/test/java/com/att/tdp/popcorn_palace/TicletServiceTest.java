package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.dto.TicketRequestDto;
import com.att.tdp.popcorn_palace.dto.TicketResponseDto;
import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.model.Ticket;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TicketRepository;
import com.att.tdp.popcorn_palace.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTests {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void bookTicket_WithValidData_ShouldReturnBookingId() {
        TicketRequestDto request = TicketRequestDto.builder()
                .showtimeId(1L)
                .seatNumber(5)
                .userId("user123")
                .build();

        Showtime showtime = new Showtime();
        showtime.setId(1L);

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 5)).thenReturn(false);

        TicketResponseDto response = ticketService.bookTicket(request);

        assertThat(response).isNotNull();
        assertThat(response.getBookingId()).isNotBlank();

        verify(showtimeRepository).findById(1L);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void bookTicket_WhenShowtimeNotFound_ShouldThrowAppException() {
        TicketRequestDto request = TicketRequestDto.builder()
                .showtimeId(99L)
                .seatNumber(10)
                .userId("user123")
                .build();

        when(showtimeRepository.findById(99L)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> ticketService.bookTicket(request));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.SHOWTIME_NOT_FOUND);
        assertThat(exception.getStatus().value()).isEqualTo(404);
    }

    @Test
    void bookTicket_WhenSeatAlreadyBooked_ShouldThrowAppException() {
        TicketRequestDto request = TicketRequestDto.builder()
                .showtimeId(1L)
                .seatNumber(5)
                .userId("user123")
                .build();

        Showtime showtime = new Showtime();
        showtime.setId(1L);

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(ticketRepository.existsByShowtimeIdAndSeatNumber(1L, 5)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> ticketService.bookTicket(request));

        assertThat(exception.getErrorType()).isEqualTo(ErrorType.SEAT_ALREADY_BOOKED);
        assertThat(exception.getStatus().value()).isEqualTo(409);
    }
}
