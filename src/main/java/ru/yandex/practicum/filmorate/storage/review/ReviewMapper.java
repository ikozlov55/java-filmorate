package ru.yandex.practicum.filmorate.storage.review;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class ReviewMapper implements RowMapper<Review> {
    @Getter
    private static final ReviewMapper instance = new ReviewMapper();

    private ReviewMapper() {
    }

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Review(rs.getInt("id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("useful"));
    }
}
