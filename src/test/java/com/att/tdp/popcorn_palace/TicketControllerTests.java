package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.controller.TicketController;
import com.att.tdp.popcorn_palace.dto.TicketRequestDto;
import com.att.tdp.popcorn_palace.dto.TicketResponseDto;
import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc
class TicketControllerTests {

    @MockBean
    private TicketService ticketService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void bookTicket_WithValidData_ShouldReturnCreated() throws Exception {
        TicketRequestDto requestDto = TicketRequestDto.builder()
                .showtimeId(1L)
                .seatNumber(5)
                .userId("user123")
                .build();

        TicketResponseDto responseDto = TicketResponseDto.builder()
                .bookingId("abc-123")
                .build();

        when(ticketService.bookTicket(any(TicketRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value("abc-123"));

        verify(ticketService).bookTicket(any(TicketRequestDto.class));
    }

    @Test
    void bookTicket_WithMissingUserId_ShouldReturnBadRequest() throws Exception {
        TicketRequestDto requestDto = TicketRequestDto.builder()
                .showtimeId(1L)
                .seatNumber(5)
                .userId("")
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).bookTicket(any(TicketRequestDto.class));
    }

    @Test
    void bookTicket_WithNegativeSeatNumber_ShouldReturnBadRequest() throws Exception {
        TicketRequestDto requestDto = TicketRequestDto.builder()
                .showtimeId(1L)
                .seatNumber(-3)
                .userId("user123")
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).bookTicket(any(TicketRequestDto.class));
    }

    @Test
    void bookTicket_WhenSeatAlreadyBooked_ShouldReturnConflict() throws Exception {
        TicketRequestDto requestDto = TicketRequestDto.builder()
                .showtimeId(1L)
                .seatNumber(5)
                .userId("user123")
                .build();

        when(ticketService.bookTicket(any(TicketRequestDto.class))).thenThrow(
                new AppException("Seat already booked", HttpStatus.CONFLICT, ErrorType.SEAT_ALREADY_BOOKED)
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorType").value("SEAT_ALREADY_BOOKED"));

        verify(ticketService).bookTicket(any(TicketRequestDto.class));
    }
}
