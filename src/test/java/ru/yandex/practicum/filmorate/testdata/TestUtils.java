package ru.yandex.practicum.filmorate.testdata;

import org.junit.jupiter.api.Assertions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Set;
import java.util.stream.Collectors;

public class TestUtils {
    public static Integer[] genresIds(Film film) {
        return film.getGenres().stream().map(Genre::getId).toArray(Integer[]::new);
    }

    public static void compareGenres(Film film1, Film film2) {
        Set<Integer> genresIds1 = film1.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Integer> genresIds2 = film2.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Assertions.assertEquals(genresIds1, genresIds2);
    }
}
