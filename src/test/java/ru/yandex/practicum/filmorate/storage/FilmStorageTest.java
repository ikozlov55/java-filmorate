package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.FilmorateJdbcConfig;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.friend_requests.FriendRequestDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.testdata.FilmBuilder;
import ru.yandex.practicum.filmorate.testdata.TestUtils;
import ru.yandex.practicum.filmorate.testdata.UserBuilder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        GenreDbStorage.class,
        MpaDbStorage.class,
        UserDbStorage.class,
        FriendRequestDbStorage.class,
        FilmorateJdbcConfig.class,
        DirectorDbStorage.class,
})
public class FilmStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;


    @Test
    public void getFilmById() {
        Film filmInput = filmStorage.create(new FilmBuilder().build());

        Film film = filmStorage.getById(filmInput.getId());

        Assertions.assertEquals(filmInput.getId(), film.getId());
        Assertions.assertEquals(filmInput.getName(), film.getName());
        Assertions.assertEquals(filmInput.getDescription(), film.getDescription());
        Assertions.assertEquals(filmInput.getReleaseDate(), film.getReleaseDate());
        Assertions.assertEquals(filmInput.getDuration(), film.getDuration());
        Assertions.assertEquals(0, film.getLikes());
        TestUtils.compareGenres(filmInput, film);
        Assertions.assertEquals(filmInput.getMpa().getId(), film.getMpa().getId());
    }

    @Test
    public void getAllFilms() {
        Film filmInput = filmStorage.create(new FilmBuilder().build());
        List<Film> filmsInput = List.of(
                filmInput,
                filmStorage.create(new FilmBuilder().build()),
                filmStorage.create(new FilmBuilder().build())
        );

        Collection<Film> films = filmStorage.getAll();

        Assertions.assertTrue(films.size() >= filmsInput.size());
        Assertions.assertTrue(films.containsAll(filmsInput));
        Film film = films.stream().filter(f -> f.getId().equals(filmInput.getId())).findFirst().get();
        Assertions.assertEquals(filmInput.getId(), film.getId());
        Assertions.assertEquals(filmInput.getName(), film.getName());
        Assertions.assertEquals(filmInput.getDescription(), film.getDescription());
        Assertions.assertEquals(filmInput.getReleaseDate(), film.getReleaseDate());
        Assertions.assertEquals(filmInput.getDuration(), film.getDuration());
        Assertions.assertEquals(filmInput.getLikes(), film.getLikes());
        TestUtils.compareGenres(filmInput, film);
        Assertions.assertEquals(filmInput.getMpa().getId(), film.getMpa().getId());
    }

    @Test
    public void filmCreate() {
        Film filmInput = new FilmBuilder().build();

        Film film = filmStorage.create(filmInput);

        Assertions.assertTrue(film.getId() > 0);
        Assertions.assertEquals(filmInput.getName(), film.getName());
        Assertions.assertEquals(filmInput.getDescription(), film.getDescription());
        Assertions.assertEquals(filmInput.getReleaseDate(), film.getReleaseDate());
        Assertions.assertEquals(filmInput.getDuration(), film.getDuration());
        Assertions.assertEquals(0, film.getLikes());
        TestUtils.compareGenres(filmInput, film);
        Assertions.assertEquals(filmInput.getMpa().getId(), film.getMpa().getId());
    }

    @Test
    public void filmCreateWithoutGenres() {
        Film filmInput = new FilmBuilder().genres().build();

        Film film = filmStorage.create(filmInput);

        Assertions.assertTrue(film.getId() > 0);
        Assertions.assertEquals(filmInput.getName(), film.getName());
        Assertions.assertEquals(filmInput.getDescription(), film.getDescription());
        Assertions.assertEquals(filmInput.getReleaseDate(), film.getReleaseDate());
        Assertions.assertEquals(filmInput.getDuration(), film.getDuration());
        Assertions.assertEquals(0, film.getLikes());
        Assertions.assertTrue(film.getGenres().isEmpty());
        Assertions.assertEquals(filmInput.getMpa().getId(), film.getMpa().getId());
    }

    @Test
    public void filmUpdate() {
        int filmId = filmStorage.create(new FilmBuilder().build()).getId();
        Film filmInput = new FilmBuilder()
                .id(filmId)
                .name("New name")
                .description("New description")
                .releaseDate(LocalDate.now())
                .duration(60)
                .mpa(3)
                .genres(1, 2, 3).build();

        Film film = filmStorage.update(filmInput);

        Assertions.assertEquals(filmInput.getId(), film.getId());
        Assertions.assertEquals(filmInput.getName(), film.getName());
        Assertions.assertEquals(filmInput.getDescription(), film.getDescription());
        Assertions.assertEquals(filmInput.getReleaseDate(), film.getReleaseDate());
        Assertions.assertEquals(filmInput.getDuration(), film.getDuration());
        Assertions.assertEquals(null, film.getLikes());
        TestUtils.compareGenres(filmInput, film);
        Assertions.assertEquals(filmInput.getMpa().getId(), film.getMpa().getId());
    }

    @Test
    public void filmDelete() {
        Film filmInput = filmStorage.create(new FilmBuilder().build());

        Film film = filmStorage.delete(filmInput);

        Assertions.assertEquals(filmInput.getId(), film.getId());
        Assertions.assertThrows(NotFoundException.class, () -> filmStorage.getById(film.getId()));
    }

    @Test
    public void addLike() {
        Film film = filmStorage.create(new FilmBuilder().build());
        User user = userStorage.create(new UserBuilder().build());

        filmStorage.addLike(film.getId(), user.getId());

        Assertions.assertEquals(filmStorage.getById(film.getId()).getLikes(), 1);
    }

    @Test
    public void deleteLike() {
        Film film = filmStorage.create(new FilmBuilder().build());
        User user = userStorage.create(new UserBuilder().build());

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.deleteLike(film.getId(), user.getId());

        Assertions.assertEquals(filmStorage.getById(film.getId()).getLikes(), 0);
    }

    @Test
    public void filmsPopular() {
        int usersCount = 10;
        int filmsCount = 5;
        int[] userIds = new int[usersCount];
        for (int i = 0; i < usersCount; i++) {
            userIds[i] = userStorage.create(new UserBuilder().build()).getId();
        }
        for (int i = 0; i < filmsCount; i++) {
            int filmId = filmStorage.create(new FilmBuilder().build()).getId();
            for (int j = 0; j < usersCount - i; j++) {
                filmStorage.addLike(filmId, userIds[j]);
            }
        }
        List<Film> films = filmStorage.filmsPopular(filmsCount).stream().toList();

        Assertions.assertEquals(filmsCount, films.size());
        for (int i = 0; i < filmsCount; i++) {
            Assertions.assertEquals(usersCount - i, films.get(i).getLikes());
        }
    }
}
