package com.att.tdp.popcorn_palace.repository;

import com.att.tdp.popcorn_palace.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByShowtimeIdAndSeatNumber(Long showtimeId, int seatNumber);

}
