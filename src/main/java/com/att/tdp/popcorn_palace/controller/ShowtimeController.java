package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.dto.ShowtimeRequestDto;
import com.att.tdp.popcorn_palace.dto.ShowtimeResponseDto;
import com.att.tdp.popcorn_palace.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/showtimes")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping("/{id}")
    public ShowtimeResponseDto getShowtimeById(@PathVariable Long id) {
        return showtimeService.getShowtimeById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ShowtimeResponseDto addShowtime(@Valid @RequestBody ShowtimeRequestDto showtimeRequestDto) {
        return showtimeService.addShowtime(showtimeRequestDto);
    }

    @PostMapping("/update/{id}")
    public ShowtimeResponseDto updateShowtime(@PathVariable Long id,
                                               @RequestBody @Valid ShowtimeRequestDto showtimeRequestDto) {
        return showtimeService.updateShowtime(id, showtimeRequestDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.ok().build();
    }
}
