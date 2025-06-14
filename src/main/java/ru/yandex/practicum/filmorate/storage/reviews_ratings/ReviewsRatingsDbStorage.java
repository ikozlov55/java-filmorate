package ru.yandex.practicum.filmorate.storage.reviews_ratings;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewsRatingsDbStorage implements ReviewsRatingsStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void upsert(int userId, int reviewId, int score) {
        String query = """
                MERGE INTO users_reviews_ratings (user_id, review_id, score)
                       KEY (user_id, review_id)
                    VALUES (?, ?, ?)
                """;
        jdbcTemplate.update(query, userId, reviewId, score);
    }

    @Override
    public boolean userReviewRatingExists(int userId, int reviewId) {
        String query = "SELECT EXISTS (SELECT 1 FROM users_reviews_ratings WHERE user_id = ? AND review_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, userId, reviewId);
        return exists != null && exists;
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
