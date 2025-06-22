package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Collection<Review> getAll(Integer filmId, int count);

    Review getById(int id);

    Review create(Review review);

    Review update(Review review);

    void delete(int reviewId);

    void deleteByFilmId(int filmId);

    void deleteByUserId(int userId);

    void addLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void deleteReviewRating(int reviewId, int userId);

    void checkReviewExists(int id);
}
