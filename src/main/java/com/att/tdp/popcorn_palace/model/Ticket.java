package com.att.tdp.popcorn_palace.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"showtime_id", "seat_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "showtime_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticket_showtimes"))
    private Long showtimeId;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private String bookingId;
}











