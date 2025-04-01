package com.att.tdp.popcorn_palace.service;

import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.dto.TicketRequestDto;
import com.att.tdp.popcorn_palace.dto.TicketResponseDto;
import com.att.tdp.popcorn_palace.model.Showtime;
import com.att.tdp.popcorn_palace.model.Ticket;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import com.att.tdp.popcorn_palace.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;

    public TicketService(TicketRepository ticketRepository, ShowtimeRepository showtimeRepository) {
        this.ticketRepository = ticketRepository;
        this.showtimeRepository = showtimeRepository;
    }


    @Transactional
    public TicketResponseDto bookTicket(TicketRequestDto ticketRequestDto) {

        // Verify showtime existence, throw an exception if the showtime is not found
        final Showtime showtime = showtimeRepository.findById(ticketRequestDto.getShowtimeId())
                .orElseThrow(() -> new AppException(
                        "Showtime not found with ID '" + ticketRequestDto.getShowtimeId() + "'",
                        HttpStatus.NOT_FOUND,
                        ErrorType.SHOWTIME_NOT_FOUND
                ));

        // Verify seat availability, throwing an exception if the seat is already booked
        if (ticketRepository.existsByShowtimeIdAndSeatNumber(
                ticketRequestDto.getShowtimeId(), ticketRequestDto.getSeatNumber())) {
            throw new AppException(
                    "Seat " + ticketRequestDto.getSeatNumber() + " is already booked for this showtime",
                    HttpStatus.CONFLICT,
                    ErrorType.SEAT_ALREADY_BOOKED
            );
        }

        final String bookingId = UUID.randomUUID().toString();

        final Ticket ticket = Ticket.builder()
                .showtimeId(showtime.getId())
                .seatNumber(ticketRequestDto.getSeatNumber())
                .userId(ticketRequestDto.getUserId())
                .bookingId(bookingId)
                .build();

        ticketRepository.save(ticket);

        return TicketResponseDto.builder()
                .bookingId(bookingId)
                .build();
    }

}