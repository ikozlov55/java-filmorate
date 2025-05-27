package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres", GenreMapper.getInstance());
    }

    @Override
    public Genre getById(int id) {
        checkGenreExists(id);
        return jdbcTemplate.queryForObject("SELECT id, name FROM genres WHERE id = ?", GenreMapper.getInstance(), id);
    }

    @Override
    public void checkGenreExists(int id) {
        String query = "SELECT EXISTS (SELECT 1 FROM genres WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, id);
        if (exists == null || !exists) {
            String reason = String.format("genre with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }
}
