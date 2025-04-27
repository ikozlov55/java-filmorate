package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    public Film create(Film film) {
        log.info("Film create request received: {}", film);
        Film createdFilm = filmStorage.create(film);
        log.info("Film created successfully: {}", film);
        return createdFilm;
    }

    public Film update(Film film) {
        log.info("Film update request received {}", film);
        if (film.getId() == null) {
            String reason = "id field is required";
            log.warn("Validation failed: {}", reason);
            throw new ValidationException(reason);
        }
        Film updatedFilm = filmStorage.update(film);
        log.info("Film updated successfully: {}", updatedFilm);
        return updatedFilm;
    }


    public Film delete(Film film) {
        log.info("Film delete request received {}", film);
        Film deletedFilm = filmStorage.delete(film);
        log.info("Film deleted successfully: {}", deletedFilm);
        return deletedFilm;
    }

    public void addLike(int filmId, int userId) {
        userStorage.getById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        userStorage.getById(userId);
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> filmsPopular(Integer count) {
        return filmStorage.filmsPopular(count != null ? count : 10);
    }
}
