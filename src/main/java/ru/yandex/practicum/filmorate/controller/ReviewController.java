package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;


@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;


    @GetMapping
    public Collection<Review> getAll(@RequestParam(required = false) Integer filmId,
                                     @RequestParam(required = false) Integer count) {
        return reviewService.getAll(filmId, count);
    }

    @GetMapping("/{reviewId}")
    public Review getById(@PathVariable int reviewId) {
        return reviewService.getById(reviewId);
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        return reviewService.update(review);
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable int reviewId) {
        reviewService.delete(reviewId);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public void addLike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public void addDislike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public void deleteLike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.deleteLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public void deleteDislike(@PathVariable int reviewId, @PathVariable int userId) {
        reviewService.deleteDislike(reviewId, userId);
    }
}
