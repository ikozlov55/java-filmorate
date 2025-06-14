package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAll();

    Film getById(int id);

    Film create(Film film);

    Film update(Film film);

    void delete(int filmId);

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    Collection<Film> filmsPopular(Integer count);

    Collection<Film> filmsCommon(int userId, int friendId);

    void checkFilmExists(int id);
}
