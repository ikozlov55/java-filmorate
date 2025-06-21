package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.reviews_ratings.ReviewsRatingsStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ReviewsRatingsStorage reviewsRatingsStorage;
    private final SimpleJdbcInsert reviewsJdbcInsert;
    private final FeedDbStorage feedDbStorage;
    private static final String SELECT_REVIEWS_QUERY = """
               SELECT r.id,
                      r.content,
                      r.is_positive,
                      r.user_id,
                      r.film_id,
                      COALESCE(SUM(urr.score), 0) AS useful
                 FROM reviews r
            LEFT JOIN users_reviews_ratings urr ON r.id = urr.review_id
                      %s
                GROUP BY r.id
                ORDER BY useful DESC
                      %s
            """;
    private static final int REVIEW_LIKE_SCORE = 1;
    private static final int REVIEW_DISLIKE_SCORE = -1;

    @Override
    public Collection<Review> getAll(Integer filmId, int count) {
        String query = String.format(SELECT_REVIEWS_QUERY,
                filmId != null ? String.format("WHERE r.film_id = %d", filmId) : "",
                "LIMIT ?"
        );
        return jdbcTemplate.query(query, ReviewMapper.getInstance(), count);
    }

    @Override
    public Review getById(int id) {
        checkReviewExists(id);
        String query = String.format(SELECT_REVIEWS_QUERY, "WHERE r.id = ?", "");
        return jdbcTemplate.queryForObject(query, ReviewMapper.getInstance(), id);
    }

    @Override
    public Review create(Review review) {
        userStorage.checkUserExists(review.getUserId());
        filmStorage.checkFilmExists(review.getFilmId());
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("content", review.getContent());
        argsMap.put("is_positive", review.getIsPositive());
        argsMap.put("user_id", review.getUserId());
        argsMap.put("film_id", review.getFilmId());
        int reviewId = reviewsJdbcInsert.executeAndReturnKey(argsMap).intValue();
        feedDbStorage.addEvent(new FeedEvent(review.getUserId(), FeedEvent.EventType.REVIEW, FeedEvent.Operation.ADD, reviewId));

        return getById(reviewId);
    }

    @Override
    public Review update(Review review) {
        checkReviewExists(review.getReviewId());
        Integer originalUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM reviews WHERE id = ?",
                Integer.class,
                review.getReviewId()
        );
        userStorage.checkUserExists(originalUserId);
        feedDbStorage.addEvent(new FeedEvent(originalUserId, FeedEvent.EventType.REVIEW, FeedEvent.Operation.UPDATE, review.getReviewId()));

        jdbcTemplate.update("""
                UPDATE reviews
                   SET content = ?,
                       is_positive = ?
                 WHERE id = ?
                """, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getById(review.getReviewId());
    }

    @Override
    @Transactional
    public void delete(int reviewId) {
        checkReviewExists(reviewId);
        reviewsRatingsStorage.deleteByReviewId(reviewId);


        Integer userId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM reviews WHERE id = ?",
                Integer.class,
                reviewId
        );

        feedDbStorage.addEvent(new FeedEvent(userId, FeedEvent.EventType.REVIEW, FeedEvent.Operation.REMOVE, reviewId));

        jdbcTemplate.update("DELETE from reviews WHERE id = ?", reviewId);
    }

    @Override
    @Transactional
    public void deleteByFilmId(int filmId) {
        filmStorage.checkFilmExists(filmId);
        reviewsRatingsStorage.deleteByFilmId(filmId);
        jdbcTemplate.update("DELETE from reviews WHERE film_id = ?", filmId);
    }

    @Override
    @Transactional
    public void deleteByUserId(int userId) {
        userStorage.checkUserExists(userId);
        reviewsRatingsStorage.deleteByUserId(userId);
        jdbcTemplate.update("DELETE from reviews WHERE user_id = ?", userId);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        checkReviewExists(reviewId);
        userStorage.checkUserExists(userId);
        reviewsRatingsStorage.upsert(userId, reviewId, REVIEW_LIKE_SCORE);
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        checkReviewExists(reviewId);
        userStorage.checkUserExists(userId);
        reviewsRatingsStorage.upsert(userId, reviewId, REVIEW_DISLIKE_SCORE);
    }

    @Override
    public void deleteReviewRating(int reviewId, int userId) {
        checkReviewExists(reviewId);
        userStorage.checkUserExists(userId);
        if (!reviewsRatingsStorage.userReviewRatingExists(userId, reviewId)) {
            String reason = String.format("rating of review with id %d by user with id %d not found", reviewId, userId);
            throw new NotFoundException(reason);
        }
        reviewsRatingsStorage.delete(userId, reviewId);
    }

    @Override
    public void checkReviewExists(int id) {
        String query = "SELECT EXISTS (SELECT 1 FROM reviews WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, id);
        if (exists == null || !exists) {
            String reason = String.format("review with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }
}
