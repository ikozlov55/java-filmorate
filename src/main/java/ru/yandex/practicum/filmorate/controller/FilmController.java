package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{filmId}")
    public Film getById(@PathVariable int filmId) {
        return filmService.getById(filmId);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmService.update(film);
    }

    @DeleteMapping("/{filmId}")
    public void delete(@PathVariable int filmId) {
        filmService.delete(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable int filmId, @PathVariable int userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(@PathVariable int filmId, @PathVariable int userId) {
        filmService.deleteLike(filmId, userId);
    }

    // GET /films/popular?count={limit}&genreId={genreId}&year={year}
    @GetMapping("/popular")
    public Collection<Film> filmsPopular(
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) Integer count) {
        return filmService.filmsPopular(genreId, year, count);
    }

    @GetMapping("/search")
    public Collection<Film> filmsSearch(@RequestParam String query, @RequestParam String by) {
        return filmService.filmSearch(query, by);
    }

    //Возвращает список фильмов режиссера отсортированных по количеству лайков или году выпуска
    //Пример запроса: GET /films/director/1?sortBy=likes
    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsOfDirectors(@PathVariable int directorId,
                                                @RequestParam(name = "sortBy", defaultValue = "year")
                                                String sortBy) {
        return filmService.getFilmsOfDirectors(directorId, sortBy);
    }
}

