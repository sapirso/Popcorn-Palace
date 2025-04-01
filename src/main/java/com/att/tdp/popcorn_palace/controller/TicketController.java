package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.TicketRequestDto;
import com.att.tdp.popcorn_palace.dto.TicketResponseDto;
import com.att.tdp.popcorn_palace.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/bookings")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public TicketResponseDto bookings(@Valid @RequestBody TicketRequestDto ticketRequestDto) {
        return ticketService.bookTicket(ticketRequestDto);
    }

}