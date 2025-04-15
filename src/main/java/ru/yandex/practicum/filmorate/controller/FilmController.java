package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static int nextEntityId = 1;
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Film create request received: {}", film);
        film.setId(nextEntityId);
        films.put(film.getId(), film);
        nextEntityId++;
        log.info("Film created successfully: {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Film update request received {}", film);
        if (film.getId() == null) {
            String reason = "id field is required";
            log.warn("Validation failed: {}", reason);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
        if (!films.containsKey(film.getId())) {
            String reason = String.format("film with id %d not found", film.getId());
            log.warn("Validation failed: {}", reason);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }
        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());
        log.info("Film updated successfully: {}", oldFilm);
        return oldFilm;
    }
}
