package com.att.tdp.popcorn_palace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieResponseDto {
    private Long id;
    private String title;
    private String genre;
    private Integer duration;
    private Double rating;
    private Integer releaseYear;
}
