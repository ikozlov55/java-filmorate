package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.testdata.FilmBuilder;
import ru.yandex.practicum.filmorate.testdata.FilmorateApi;
import ru.yandex.practicum.filmorate.testdata.TestUtils;
import ru.yandex.practicum.filmorate.testdata.UserBuilder;
import ru.yandex.practicum.filmorate.validation.ReleaseDateValidator;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.testdata.Matchers.validationError;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Import(FilmorateApi.class)
public class FilmControllerTest {
    @Autowired
    private FilmorateApi filmorateApi;


    @Test
    void getFilmById() throws Exception {
        Film film = new FilmBuilder().build();
        int filmId = filmorateApi.createAndGetId(film);

        filmorateApi.getFilmById(filmId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.likes").value(0))
                .andExpect(jsonPath("$.mpa.id").value(film.getMpa().getId()))
                .andExpect(jsonPath("$.genres", hasSize(film.getGenres().size())))
                .andExpect(jsonPath("$.genres..id", hasItems(TestUtils.genresIds(film))));
    }


    @Test
    void filmIdMustExistOnGetFilmById() throws Exception {
        filmorateApi.getFilmById(999)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("film with id 999 not found"));
    }

    @Test
    void getAllFilms() throws Exception {
        filmorateApi.create(new FilmBuilder().build());

        filmorateApi.getAllFilms().andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void filmCreate() throws Exception {
        Film film = new FilmBuilder().build();
        ResultActions ret = filmorateApi.create(film);
        filmorateApi.create(film).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.likes").value(0))
                .andExpect(jsonPath("$.mpa.id").value(film.getMpa().getId()))
                .andExpect(jsonPath("$.genres", hasSize(film.getGenres().size())))
                .andExpect(jsonPath("$.genres..id", hasItems(TestUtils.genresIds(film))));
    }

    @Test
    void filmUpdate() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        Film film = new FilmBuilder()
                .id(filmId)
                .name("New name")
                .description("New description")
                .releaseDate(LocalDate.now())
                .duration(60)
                .mpa(3)
                .genres(1, 2, 3).build();

        filmorateApi.update(film).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(filmId))
                .andExpect(jsonPath("$.name").value(film.getName()))
                .andExpect(jsonPath("$.description").value(film.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(film.getDuration()))
                .andExpect(jsonPath("$.likes").value(0))
                .andExpect(jsonPath("$.mpa.id").value(film.getMpa().getId()))
                .andExpect(jsonPath("$.genres").isArray())
                .andExpect(jsonPath("$.genres", hasSize(film.getGenres().size())))
                .andExpect(jsonPath("$.genres..id", hasItems(TestUtils.genresIds(film))));
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
    void mpaIsRequired() throws Exception {
        Film film = new FilmBuilder().build();
        film.setMpa(null);

        filmorateApi.create(film).andExpect(status().isBadRequest())
                .andExpect(validationError("mpa", "must not be null"));
    }

    @Test
    void mpaIdMustExistOnFilmCreate() throws Exception {
        Film film = new FilmBuilder().mpa(99).build();

        filmorateApi.create(film).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("mpa with id 99 not found"));
    }

    @Test
    void genreIdMustExistOnFilmCreate() throws Exception {
        Film film = new FilmBuilder().genres(1, 2, 99).build();

        filmorateApi.create(film).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("genre with id 99 not found"));
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

    @Test
    void filmsPopular() throws Exception {
        int usersCount = 10;
        int filmsCount = 5;
        int[] userIds = new int[usersCount];
        for (int i = 0; i < usersCount; i++) {
            userIds[i] = filmorateApi.createAndGetId(new UserBuilder().build());
        }
        for (int i = 0; i < filmsCount; i++) {
            int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
            for (int j = 0; j < usersCount - i; j++) {
                filmorateApi.addLike(filmId, userIds[j]);
            }
        }
        filmorateApi.filmsPopular(filmsCount).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(filmsCount)))
                .andExpect(jsonPath("$[0].likes").value(usersCount))
                .andExpect(jsonPath("$[1].likes").value(usersCount - 1))
                .andExpect(jsonPath("$[2].likes").value(usersCount - 2))
                .andExpect(jsonPath("$[3].likes").value(usersCount - 3))
                .andExpect(jsonPath("$[4].likes").value(usersCount - 4));
    }

}
