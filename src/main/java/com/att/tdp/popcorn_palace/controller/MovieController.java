package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.MovieRequestDto;
import com.att.tdp.popcorn_palace.dto.MovieResponseDto;
import com.att.tdp.popcorn_palace.dto.MovieUpdateRequestDto;
import com.att.tdp.popcorn_palace.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/all")
    public List<MovieResponseDto> getAllMovies() {
        return movieService.getAllMovies();
    }


    @PostMapping("")
    public MovieResponseDto addMovie(@Valid @RequestBody MovieRequestDto movieDto) {
        return movieService.addMovie(movieDto);
    }

    @PostMapping("/update/{title}")
    public ResponseEntity<MovieResponseDto> updateMovie(
            @PathVariable String title,
            @Valid @RequestBody MovieUpdateRequestDto updateDto) {
        return ResponseEntity.ok(movieService.updateMovieByTitle(title, updateDto));
    }

    @DeleteMapping("/{title}")
    public ResponseEntity<Void> deleteMovie(@PathVariable String title) {
        movieService.deleteMovieByTitle(title);
        return ResponseEntity.ok().build();
    }


}
