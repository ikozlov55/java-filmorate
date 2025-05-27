package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MpaMapper implements RowMapper<Mpa> {

    @Getter
    private static final MpaMapper instance = new MpaMapper();

    private MpaMapper() {
    }

    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa rating = new Mpa();
        rating.setId(rs.getInt("id"));
        rating.setName(rs.getString("name"));
        return rating;
    }
}
