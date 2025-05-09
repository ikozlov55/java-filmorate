package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.testdata.FilmBuilder;
import ru.yandex.practicum.filmorate.testdata.FilmorateApi;
import ru.yandex.practicum.filmorate.testdata.UserBuilder;
import ru.yandex.practicum.filmorate.validation.ReleaseDateValidator;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.testdata.Matchers.validationError;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FilmorateApi.class)
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilmorateApi filmorateApi;


    @Test
    void filmCreate() throws Exception {
        Film film = new FilmBuilder().build();

        filmorateApi.create(film).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.likes").value(0));
    }

    @Test
    void filmUpdate() throws Exception {
        Film film = new FilmBuilder().build();
        int filmId = filmorateApi.createAndGetId(film);
        film.setId(filmId);
        film.setName("New name");
        film.setDescription("New description");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(60);

        filmorateApi.update(film).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.likes").value(0));
    }

    @Test
    void filmLikesCannotBeUpdated() throws Exception {
        Film film = new FilmBuilder().build();
        int filmId = filmorateApi.createAndGetId(film);
        film.setId(filmId);
        film.setName("New name");
        film.setLikes(99);

        filmorateApi.update(film).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.likes").value(0));
    }

    @Test
    void idIsRequiredOnFilmUpdate() throws Exception {
        Film film = new FilmBuilder().build();

        filmorateApi.update(film).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("id field is required"));
    }

    @Test
    void idMustExistOnFilmUpdate() throws Exception {
        Film film = new FilmBuilder().id(99).build();

        filmorateApi.update(film).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("film with id 99 not found"));
    }

    @Test
    void nameIsRequired() throws Exception {
        Film film = new FilmBuilder().name(null).build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("name", "must not be blank"));
    }

    @Test
    void nameCannotBeEmpty() throws Exception {
        Film film = new FilmBuilder().name("").build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("name", "must not be blank"));
    }

    @Test
    void descriptionIsRequired() throws Exception {
        Film film = new FilmBuilder().description(null).build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("description", "must not be blank"));
    }

    @Test
    void descriptionHasSizeLimit() throws Exception {
        Film film = new FilmBuilder().description("x".repeat(201)).build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("description", "size must be between 0 and 200"));
    }

    @Test
    void releaseDateIsRequired() throws Exception {
        Film film = new FilmBuilder().releaseDate(null).build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("releaseDate",
                        "release date must be greater then december 28, 1985"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 0})
    void releaseDateHasLowerLimit(int offset) throws Exception {
        LocalDate date = ReleaseDateValidator.MIN_DATE.minusDays(offset);
        Film film = new FilmBuilder().releaseDate(date).build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("releaseDate",
                        "release date must be greater then december 28, 1985"));
    }

    @Test
    void durationIsRequired() throws Exception {
        Film film = new FilmBuilder().duration(null).build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("duration", "must not be null"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void durationMustBePositive(int duration) throws Exception {
        Film film = new FilmBuilder().duration(duration).build();

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("duration", "must be greater than 0"));
    }

    @Test
    void addLike() throws Exception {
        int userId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int userId2 = filmorateApi.createAndGetId(new UserBuilder().build());
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());

        filmorateApi.addLike(filmId, userId1).andExpect(status().isOk());
        filmorateApi.addLike(filmId, userId2).andExpect(status().isOk());
        filmorateApi.getFilmById(filmId)
                .andExpect(jsonPath("$.likes").value(2));
    }

    @Test
    void repeatedAddLikeBySameUserDoesNotIncreaseLikes() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());

        filmorateApi.addLike(filmId, userId).andExpect(status().isOk());
        filmorateApi.addLike(filmId, userId).andExpect(status().isOk());
        filmorateApi.getFilmById(filmId)
                .andExpect(jsonPath("$.likes").value(1));
    }

    @Test
    void filmIdMustExistOnAddLike() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.addLike(999, userId).andExpect(status().isNotFound());
    }

    @Test
    void userIdMustExistOnAddLike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());

        filmorateApi.addLike(filmId, 999).andExpect(status().isNotFound());
    }

    @Test
    void deleteLike() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());

        filmorateApi.addLike(filmId, userId);
        filmorateApi.deleteLike(filmId, userId).andExpect(status().isOk());
        filmorateApi.getFilmById(filmId)
                .andExpect(jsonPath("$.likes").value(0));
    }

    @Test
    void repeatedDeleteLikeDoesNotDecreaseLikes() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());

        filmorateApi.addLike(filmId, userId);
        filmorateApi.deleteLike(filmId, userId).andExpect(status().isOk());
        filmorateApi.deleteLike(filmId, userId).andExpect(status().isOk());
        filmorateApi.getFilmById(filmId)
                .andExpect(jsonPath("$.likes").value(0));
    }

    @Test
    void filmIdMustExistOnDeleteLike() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.deleteLike(999, userId).andExpect(status().isNotFound());
    }

    @Test
    void userIdMustExistOnDeleteLike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());

        filmorateApi.deleteLike(filmId, 999).andExpect(status().isNotFound());
    }

}
