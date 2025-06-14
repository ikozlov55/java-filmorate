package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
@Deprecated
public abstract class InMemoryFilmStorage implements FilmStorage {
    private static int nextEntityId = 1;
    private final Map<Integer, Film> films = new HashMap<>();
    private final Map<Integer, Set<Integer>> filmsToUsersLiked = new HashMap<>();

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Film getById(int id) {
        checkFilmExists(id);
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        film.setId(nextEntityId);
        films.put(film.getId(), film);
        nextEntityId++;
        return film;
    }

    @Override
    public Film update(Film film) {
        checkFilmExists(film.getId());
        film.setLikes(films.get(film.getId()).getLikes());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(int filmId) {
        checkFilmExists(filmId);
    }

    @Override
    public void addLike(int filmId, int userId) {
        checkFilmExists(filmId);
        if (!filmsToUsersLiked.containsKey(filmId)) {
            filmsToUsersLiked.put(filmId, new HashSet<>());
        }
        filmsToUsersLiked.get(filmId).add(userId);
        films.get(filmId).setLikes(filmsToUsersLiked.get(filmId).size());
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        checkFilmExists(filmId);
        if (!filmsToUsersLiked.containsKey(filmId)) {
            return;
        }
        filmsToUsersLiked.get(filmId).remove(userId);
        films.get(filmId).setLikes(filmsToUsersLiked.get(filmId).size());
    }

    @Override
    public Collection<Film> filmsPopular(Integer count) {
        return films.values().stream()
                .sorted(Comparator.comparing(Film::getLikes).reversed())
                .limit(count)
                .toList();
    }

    @Override
    public void checkFilmExists(int id) {
        if (!films.containsKey(id)) {
            String reason = String.format("film with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }

    @Override
    public Collection<Film> filmSearch(String query) {
        return List.of();
    }
}
