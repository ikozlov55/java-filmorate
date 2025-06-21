package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class MpaMapper implements RowMapper<Mpa> {

    @Getter
    private static final MpaMapper instance = new MpaMapper();

    private MpaMapper() {
    }

    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("id"), rs.getString("name"));
    }
}
