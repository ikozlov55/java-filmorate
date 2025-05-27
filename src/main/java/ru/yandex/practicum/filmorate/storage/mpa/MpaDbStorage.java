package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa", MpaMapper.getInstance());
    }

    @Override
    public Mpa getById(int id) {
        checkMpaExists(id);
        return jdbcTemplate.queryForObject("SELECT id, name FROM mpa WHERE id = ?", MpaMapper.getInstance(), id);
    }

    @Override
    public void checkMpaExists(int id) {
        String query = "SELECT EXISTS (SELECT 1 FROM mpa WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, id);
        if (exists == null || !exists) {
            String reason = String.format("mpa with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }
}
