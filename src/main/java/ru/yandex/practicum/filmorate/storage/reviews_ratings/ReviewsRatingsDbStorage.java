package ru.yandex.practicum.filmorate.storage.reviews_ratings;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReviewsRatingsDbStorage implements ReviewsRatingsStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert usersReviewsRatingsJdbcInsert;

    @Override
    public boolean userReviewRatingExists(int userId, int reviewId) {
        String query = "SELECT EXISTS (SELECT 1 FROM users_reviews_ratings WHERE user_id = ? AND review_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, userId, reviewId);
        return exists != null && exists;
    }

    @Override
    public void create(int userId, int reviewId, int score) {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("user_id", userId);
        argsMap.put("review_id", reviewId);
        argsMap.put("score", score);
        usersReviewsRatingsJdbcInsert.execute(argsMap);
        Boolean exists = userReviewRatingExists(userId, reviewId);
        System.out.println(exists);
    }

    @Override
    public void update(int userId, int reviewId, int score) {
        jdbcTemplate.update("""
                UPDATE users_reviews_ratings
                   SET score = ?
                 WHERE user_id = ?
                   AND review_id = ?
                """, score, userId, reviewId);
    }

    @Override
    public void delete(int userId, int reviewId) {
        jdbcTemplate.update("DELETE FROM users_reviews_ratings WHERE user_id = ? AND review_id = ?",
                userId, reviewId);
    }

    @Override
    public void deleteByUserId(int userId) {
        jdbcTemplate.update("DELETE FROM users_reviews_ratings WHERE user_id = ?", userId);
    }

    @Override
    public void deleteByReviewId(int reviewId) {
        jdbcTemplate.update("DELETE FROM users_reviews_ratings WHERE review_id = ?", reviewId);
    }

    @Override
    public void deleteByFilmId(int filmId) {
        jdbcTemplate.update("""
                DELETE
                  FROM users_reviews_ratings
                 WHERE review_id IN (
                    SELECT id
                      FROM reviews
                     WHERE film_id = ?
                 )
                """, filmId);
    }
}
