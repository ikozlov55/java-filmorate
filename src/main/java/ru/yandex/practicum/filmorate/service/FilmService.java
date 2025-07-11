package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final int DEFAULT_FILMS_POPULAR_COUNT = 10;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ReviewStorage reviewStorage;
    private final DirectorStorage directorStorage;


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

    @Transactional
    public void delete(int filmId) {
        log.info("Film delete request received {}", filmId);
        reviewStorage.deleteByFilmId(filmId);
        filmStorage.delete(filmId);
        log.info("Film deleted successfully: {}", filmId);
    }

    public void addLike(int filmId, int userId) {
        userStorage.getById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        userStorage.getById(userId);
        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> filmsPopular(Integer genreId, String year, Integer count) {
        count = count != null ? count : DEFAULT_FILMS_POPULAR_COUNT;
        return filmStorage.filmsPopular(genreId, year, count);
    }


    public Collection<Film> filmsCommon(int userId, int friendId) {
        return filmStorage.filmsCommon(userId, friendId);
    }

    public Collection<Film> filmSearch(String searchTitle, String by) {
        if (by == null || by.isEmpty()) {
            throw new IllegalArgumentException("Film search by is required");
        }

        if (searchTitle == null || searchTitle.isEmpty()) {
            return filmStorage.getAll();
        }

        boolean isDirectorSearch = by.contains("director");
        boolean isTitleSearch = by.contains("title");

        return filmStorage.filmSearch(searchTitle, isDirectorSearch, isTitleSearch).stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes(), film1.getLikes()))
                .collect(Collectors.toList());
    }

    public Collection<Film> getFilmsOfDirectors(int directorId, String sortBy) {
        log.info("Films of Directors sort by year or likes request received {}", directorStorage.getById(directorId));
        return filmStorage.getFilmsOfDirectors(directorId, sortBy);
    }
}
