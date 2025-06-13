package ru.yandex.practicum.filmorate.storage.reviews_ratings;

public interface ReviewsRatingsStorage {
    boolean userReviewRatingExists(int userId, int reviewId);

    void create(int userId, int reviewId, int score);

    void update(int userId, int reviewId, int score);

    void delete(int userId, int reviewId);

    void deleteByUserId(int userId);

    void deleteByReviewId(int reviewId);

    void deleteByFilmId(int filmId);
}
