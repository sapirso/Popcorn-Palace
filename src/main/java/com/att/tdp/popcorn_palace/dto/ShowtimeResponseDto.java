package com.att.tdp.popcorn_palace.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ShowtimeResponseDto {
    private Long id;

    private String theater;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Float price;

    private String movieTitle;
    private Integer movieReleaseYear;

}



