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

    /*
    GET /films/director/{directorId}?sortBy=[year,likes]
    Возвращает список фильмов режиссера отсортированных по количеству лайков или году выпуска
    Пример запроса: GET /films/director/1?sortBy=likes
     */
    Collection<Film> getFilmsOfDirectors(int directorId, String sortBy);

    void checkFilmExists(int id);

    Collection<Film> filmSearch(String searchTitle, boolean isDirectorSearch, boolean isTitleSearch);
}
