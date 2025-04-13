package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.testdata.FilmBuilder;
import ru.yandex.practicum.filmorate.validation.ReleaseDateValidator;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.testdata.Matchers.validationError;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void filmCreate() throws Exception {
        Film film = new FilmBuilder().build();

        create(film).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()));
    }

    @Test
    void filmUpdate() throws Exception {
        Film film = new FilmBuilder().build();
        create(film).andExpect(status().isOk());
        film.setId(1);
        film.setName("New name");
        film.setDescription("New description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(60);

        update(film).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(film.getId()))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()));
    }

    @Test
    void idIsRequiredOnFilmUpdate() throws Exception {
        Film film = new FilmBuilder().build();

        update(film).andExpect(status().isBadRequest())
                .andExpect(status().reason("id field is required"));
    }

    @Test
    void idMustExistsOnFilmUpdate() throws Exception {
        Film film = new FilmBuilder().id(99).build();

        update(film).andExpect(status().isNotFound())
                .andExpect(status().reason("film with id 99 not found"));
    }

    @Test
    void nameIsRequired() throws Exception {
        Film film = new FilmBuilder().name(null).build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("name", "must not be blank"));
    }

    @Test
    void nameCannotBeEmpty() throws Exception {
        Film film = new FilmBuilder().name("").build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("name", "must not be blank"));
    }

    @Test
    void descriptionIsRequired() throws Exception {
        Film film = new FilmBuilder().description(null).build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("description", "must not be blank"));
    }

    @Test
    void descriptionHasSizeLimit() throws Exception {
        Film film = new FilmBuilder().description("x".repeat(201)).build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("description", "size must be between 0 and 200"));
    }

    @Test
    void releaseDateIsRequired() throws Exception {
        Film film = new FilmBuilder().releaseDate(null).build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("releaseDate",
                        "release date must be greater then december 28, 1985"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 0})
    void releaseDateHasLowerLimit(int offset) throws Exception {
        LocalDate date = ReleaseDateValidator.MIN_DATE.minusDays(offset);
        Film film = new FilmBuilder().releaseDate(date).build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("releaseDate",
                        "release date must be greater then december 28, 1985"));
    }

    @Test
    void durationIsRequired() throws Exception {
        Film film = new FilmBuilder().duration(null).build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("duration", "must not be null"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void durationMustBePositive(int duration) throws Exception {
        Film film = new FilmBuilder().duration(duration).build();

        create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("duration", "must be greater than 0"));
    }

    ResultActions create(Film film) throws Exception {
        String body = objectMapper.writeValueAsString(film);
        return mockMvc.perform(post("/films")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    ResultActions update(Film film) throws Exception {
        String body = objectMapper.writeValueAsString(film);
        return mockMvc.perform(put("/films")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

}
