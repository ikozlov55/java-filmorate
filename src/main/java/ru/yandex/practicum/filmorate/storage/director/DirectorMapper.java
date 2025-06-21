package ru.yandex.practicum.filmorate.storage.director;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class DirectorMapper implements RowMapper<Director> {
    @Getter
    private static final DirectorMapper instance = new DirectorMapper();

    private DirectorMapper() {
    }

    @Override
    public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Director(rs.getInt("id"), rs.getString("name"));
    }
}
