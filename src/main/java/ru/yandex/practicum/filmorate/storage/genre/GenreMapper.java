package ru.yandex.practicum.filmorate.storage.genre;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;


public class GenreMapper implements RowMapper<Genre> {
    @Getter
    private static final GenreMapper instance = new GenreMapper();

    private GenreMapper() {
    }

    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("name"));
        return genre;
    }
}
