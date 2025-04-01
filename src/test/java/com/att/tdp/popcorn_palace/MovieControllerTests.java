package com.att.tdp.popcorn_palace;

import com.att.tdp.popcorn_palace.Exception.AppException;
import com.att.tdp.popcorn_palace.Exception.ErrorType;
import com.att.tdp.popcorn_palace.dto.MovieRequestDto;
import com.att.tdp.popcorn_palace.dto.MovieResponseDto;
import com.att.tdp.popcorn_palace.dto.MovieUpdateRequestDto;
import com.att.tdp.popcorn_palace.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerTest {

    @MockBean
    private MovieService movieService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ----- getAllMovies Tests -----

    @Test
    void getAllMovies_ShouldReturnAllMovies() throws Exception {
        MovieResponseDto movie1 = createMovieResponse(1L, "Movie 1", "Action", 120, 8.5, 2020);
        MovieResponseDto movie2 = createMovieResponse(2L, "Movie 2", "Comedy", 110, 7.5, 2021);
        List<MovieResponseDto> movies = Arrays.asList(movie1, movie2);

        when(movieService.getAllMovies()).thenReturn(movies);

        mockMvc.perform(get("/movies/all"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Movie 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Movie 2"));

        verify(movieService).getAllMovies();
    }

    @Test
    void getAllMovies_WhenNoMovies_ShouldReturnEmptyArray() throws Exception {
        when(movieService.getAllMovies()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/movies/all"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(movieService).getAllMovies();
    }

    // ----- addMovie Tests -----

    @Test
    void addMovie_WithValidData_ShouldReturnCreatedMovie() throws Exception {
        MovieRequestDto requestDto = MovieRequestDto.builder()
                .title("New Movie")
                .genre("Action")
                .duration(120)
                .rating(8.5)
                .releaseYear(2023)
                .build();

        MovieResponseDto responseDto = createMovieResponse(1L, "New Movie", "Action", 120, 8.5, 2023);

        when(movieService.addMovie(any(MovieRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Movie"))
                .andExpect(jsonPath("$.genre").value("Action"))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.rating").value(8.5))
                .andExpect(jsonPath("$.releaseYear").value(2023));

        verify(movieService).addMovie(any(MovieRequestDto.class));
    }

    @Test
    void addMovie_WithMissingTitle_ShouldReturnBadRequest() throws Exception {
        MovieRequestDto requestDto = MovieRequestDto.builder()
                .title("")  // Empty title - @NotBlank violation
                .genre("Action")
                .duration(120)
                .releaseYear(2023)
                .build();

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(movieService, never()).addMovie(any(MovieRequestDto.class));
    }

    @Test
    void addMovie_WithNegativeDuration_ShouldReturnBadRequest() throws Exception {
        MovieRequestDto requestDto = MovieRequestDto.builder()
                .title("New Movie")
                .genre("Action")
                .duration(-10)  // Negative duration - @PositiveOrZero violation
                .releaseYear(2023)
                .build();

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        verify(movieService, never()).addMovie(any(MovieRequestDto.class));
    }

    @Test
    void addMovie_WithDuplicateTitle_ShouldReturnConflict() throws Exception {
        MovieRequestDto requestDto = MovieRequestDto.builder()
                .title("Existing Movie")
                .genre("Action")
                .duration(120)
                .rating(8.5)
                .releaseYear(2023)
                .build();

        when(movieService.addMovie(any(MovieRequestDto.class))).thenThrow(
                new AppException(
                        "A movie titled 'Existing Movie' already exists in the system",
                        HttpStatus.CONFLICT,
                        ErrorType.DUPLICATE_MOVIE_TITLE
                )
        );

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict());

        verify(movieService).addMovie(any(MovieRequestDto.class));
    }

    // ----- updateMovie Tests -----

    @Test
    void updateMovie_WithValidData_ShouldReturnUpdatedMovie() throws Exception {
        String title = "Original Movie";
        MovieUpdateRequestDto requestDto = MovieUpdateRequestDto.builder()
                .title("Updated Movie")
                .genre("Updated Genre")
                .duration(130)
                .rating(9.0)
                .releaseYear(2024)
                .build();

        MovieResponseDto responseDto = createMovieResponse(1L, "Updated Movie", "Updated Genre", 130, 9.0, 2024);

        when(movieService.updateMovieByTitle(eq(title), any(MovieUpdateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/movies/update/{title}", title)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Movie"))
                .andExpect(jsonPath("$.genre").value("Updated Genre"))
                .andExpect(jsonPath("$.duration").value(130))
                .andExpect(jsonPath("$.rating").value(9.0))
                .andExpect(jsonPath("$.releaseYear").value(2024));

        verify(movieService).updateMovieByTitle(eq(title), any(MovieUpdateRequestDto.class));
    }


    @Test
    void updateMovie_WithPartialData_ShouldAcceptRequest() throws Exception {
        String title = "Original Movie";
        MovieUpdateRequestDto requestDto = MovieUpdateRequestDto.builder()
                .genre("Updated Genre")
                .rating(9.0)
                .build();

        MovieResponseDto responseDto = createMovieResponse(1L, "Original Movie", "Updated Genre", 120, 9.0, 2020);

        when(movieService.updateMovieByTitle(eq(title), any(MovieUpdateRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/movies/update/{title}", title)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Original Movie"))
                .andExpect(jsonPath("$.genre").value("Updated Genre"))
                .andExpect(jsonPath("$.rating").value(9.0));

        verify(movieService).updateMovieByTitle(eq(title), any(MovieUpdateRequestDto.class));
    }

    @Test
    void updateMovie_MovieNotFound_ShouldReturnNotFound() throws Exception {
        String title = "Non-existent Movie";
        MovieUpdateRequestDto requestDto = MovieUpdateRequestDto.builder()
                .title("Updated Movie")
                .genre("Action")
                .duration(120)
                .releaseYear(2023)
                .build();

        when(movieService.updateMovieByTitle(eq(title), any(MovieUpdateRequestDto.class)))
                .thenThrow(new AppException(
                        "Movie with title 'Non-existent Movie' not found",
                        HttpStatus.NOT_FOUND,
                        ErrorType.MOVIE_NOT_FOUND
                ));

        mockMvc.perform(post("/movies/update/{title}", title)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(movieService).updateMovieByTitle(eq(title), any(MovieUpdateRequestDto.class));
    }


    // ----- deleteMovie Tests -----

    @Test
    void deleteMovie_ExistingMovie_ShouldReturnOk() throws Exception {
        String title = "Movie To Delete";
        doNothing().when(movieService).deleteMovieByTitle(title);

        mockMvc.perform(delete("/movies/{title}", title))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        verify(movieService).deleteMovieByTitle(title);
    }

    @Test
    void deleteMovie_NonExistingMovie_ShouldReturnNotFound() throws Exception {
        String title = "Non-existent Movie";
        doThrow(new AppException(
                "Movie not found",
                HttpStatus.NOT_FOUND,
                ErrorType.MOVIE_NOT_FOUND,
                "Movie with title 'Non-existent Movie' does not exist"
        )).when(movieService).deleteMovieByTitle(title);

        mockMvc.perform(delete("/movies/{title}", title))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

        verify(movieService).deleteMovieByTitle(title);
    }

    // ----- Helper methods -----

    private MovieResponseDto createMovieResponse(Long id, String title, String genre, Integer duration, Double rating, Integer releaseYear) {
        return MovieResponseDto.builder()
                .id(id)
                .title(title)
                .genre(genre)
                .duration(duration)
                .rating(rating)
                .releaseYear(releaseYear)
                .build();
    }
}