package com.att.tdp.popcorn_palace.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String title;

    private String genre;

    private Integer duration;

    private Double rating;

    @Column(name = "release_year")
    private Integer releaseYear;

    @OneToMany(
            mappedBy = "movieId",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Showtime> showtimes = new HashSet<>();
}
