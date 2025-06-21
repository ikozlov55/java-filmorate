package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    public static final int DEFAULT_REVIEWS_COUNT = 10;
    private final ReviewStorage reviewStorage;


    public Collection<Review> getAll(Integer filmId, Integer count) {
        return reviewStorage.getAll(filmId, count != null ? count : DEFAULT_REVIEWS_COUNT);
    }


    public Review getById(int id) {
        return reviewStorage.getById(id);
    }


    public Review create(Review review) {
        log.info("Review create request received: {}", review);
        Review createdReview = reviewStorage.create(review);
        log.info("Review created successfully: {}", review);
        return createdReview;
    }


    public Review update(Review review) {
        log.info("Review update request received {}", review);
        if (review.getReviewId() == null) {
            String reason = "id field is required";
            log.warn("Validation failed: {}", reason);
            throw new ValidationException(reason);
        }
        Review updatedReview = reviewStorage.update(review);
        log.info("Review updated successfully: {}", updatedReview);
        return updatedReview;
    }


    public void delete(int reviewId) {
        reviewStorage.delete(reviewId);
    }


    public void addLike(int reviewId, int userId) {
        reviewStorage.addLike(reviewId, userId);
    }


    public void addDislike(int reviewId, int userId) {
        reviewStorage.addDislike(reviewId, userId);
    }


    public void deleteLike(int reviewId, int userId) {
        reviewStorage.deleteReviewRating(reviewId, userId);
    }


    public void deleteDislike(int reviewId, int userId) {
        reviewStorage.deleteReviewRating(reviewId, userId);
    }
}
