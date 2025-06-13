package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.testdata.FilmBuilder;
import ru.yandex.practicum.filmorate.testdata.FilmorateApi;
import ru.yandex.practicum.filmorate.testdata.ReviewBuilder;
import ru.yandex.practicum.filmorate.testdata.UserBuilder;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.testdata.Matchers.validationError;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Import(FilmorateApi.class)
public class ReviewControllerTest {
    @Autowired
    private FilmorateApi filmorateApi;

    @Test
    void getReviewById() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().filmId(filmId).userId(userId).build();
        int reviewId = filmorateApi.createAndGetId(review);

        filmorateApi.getReviewById(reviewId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.content").value(review.getContent()))
                .andExpect(jsonPath("$.isPositive").value(review.getIsPositive()))
                .andExpect(jsonPath("$.userId").value(review.getUserId()))
                .andExpect(jsonPath("$.filmId").value(review.getFilmId()))
                .andExpect(jsonPath("$.useful").value(0));
    }

    @Test
    void reviewIdMustExistOnGetReviewById() throws Exception {
        filmorateApi.getReviewById(999).andExpect(status().isNotFound());
    }

    @Test
    void getAllReviewsWithCount() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        for (int i = 0; i < 10; i++) {
            int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
            filmorateApi.create(new ReviewBuilder().filmId(filmId).userId(userId).build());
        }

        filmorateApi.getAllReviews(null, 5).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void countHasDefaultValueOnGetAllReviews() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        for (int i = 0; i < 15; i++) {
            int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
            filmorateApi.create(new ReviewBuilder().filmId(filmId).userId(userId).build());
        }

        filmorateApi.getAllReviews(null, null).andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(ReviewService.DEFAULT_REVIEWS_COUNT)));
    }

    @Test
    void getAllReviewsByFilmId() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        for (int i = 0; i < 10; i++) {
            int userId = filmorateApi.createAndGetId(new UserBuilder().build());
            filmorateApi.create(new ReviewBuilder().filmId(filmId).userId(userId).build());
        }
        filmorateApi.getAllReviews(filmId, null).andExpect(status().isOk())
                .andExpect(jsonPath("$..filmId", everyItem(equalTo(filmId))));
    }

    @Test
    void reviewCreate() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().filmId(filmId).userId(userId).build();

        filmorateApi.create(review).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(greaterThan(0)))
                .andExpect(jsonPath("$.content").value(review.getContent()))
                .andExpect(jsonPath("$.isPositive").value(review.getIsPositive()))
                .andExpect(jsonPath("$.userId").value(review.getUserId()))
                .andExpect(jsonPath("$.filmId").value(review.getFilmId()))
                .andExpect(jsonPath("$.useful").value(0));
    }

    @Test
    void userMustExistOnReviewCreate() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        Review review = new ReviewBuilder().filmId(filmId).userId(999).build();

        filmorateApi.create(review).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("user with id 999 not found"));
    }

    @Test
    void filmMustExistOnReviewCreate() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().filmId(999).userId(userId).build();

        filmorateApi.create(review).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("film with id 999 not found"));
    }

    @Test
    void contentIsRequired() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().filmId(filmId).userId(userId).content(null).build();

        filmorateApi.create(review).andExpect(status().isBadRequest())
                .andExpect(validationError("content", "must not be blank"));
    }

    @Test
    void isPositiveIsRequired() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().filmId(filmId).userId(userId).isPositive(null).build();

        filmorateApi.create(review).andExpect(status().isBadRequest())
                .andExpect(validationError("isPositive", "must not be null"));
    }

    @Test
    void userIdIsRequired() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        Review review = new ReviewBuilder().filmId(filmId).userId(null).build();

        filmorateApi.create(review).andExpect(status().isBadRequest())
                .andExpect(validationError("userId", "must not be null"));
    }

    @Test
    void filmIdIsRequired() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().filmId(null).userId(userId).build();

        filmorateApi.create(review).andExpect(status().isBadRequest())
                .andExpect(validationError("filmId", "must not be null"));
    }

    @Test
    void reviewUpdate() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review oldReview = new ReviewBuilder().filmId(filmId).userId(userId).isPositive(false).build();
        int reviewId = filmorateApi.createAndGetId(oldReview);
        Review review = new ReviewBuilder()
                .id(reviewId)
                .content("New content")
                .isPositive(true)
                .filmId(filmId)
                .userId(userId).build();

        filmorateApi.update(review).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.content").value(review.getContent()))
                .andExpect(jsonPath("$.isPositive").value(review.getIsPositive()))
                .andExpect(jsonPath("$.userId").value(review.getUserId()))
                .andExpect(jsonPath("$.filmId").value(review.getFilmId()))
                .andExpect(jsonPath("$.useful").value(0));
    }

    @Test
    void reviewUsefulCannotBeUpdated() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());
        Review review = new ReviewBuilder()
                .id(reviewId)
                .useful(99)
                .filmId(filmId)
                .userId(userId).build();

        filmorateApi.update(review).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.content").value(review.getContent()))
                .andExpect(jsonPath("$.isPositive").value(review.getIsPositive()))
                .andExpect(jsonPath("$.userId").value(review.getUserId()))
                .andExpect(jsonPath("$.filmId").value(review.getFilmId()))
                .andExpect(jsonPath("$.useful").value(0));
    }

    @Test
    void idIsRequiredOnReviewUpdate() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().filmId(filmId).userId(userId).build();

        filmorateApi.update(review).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("id field is required"));
    }

    @Test
    void idMustExistOnReviewUpdate() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        Review review = new ReviewBuilder().id(999).filmId(filmId).userId(userId).build();

        filmorateApi.update(review).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("review with id 999 not found"));
    }

    @Test
    void addLike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewLike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.getReviewById(reviewId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.useful").value(1));
    }

    @Test
    void repeatedAddLikeBySameUserDoesNotIncreaseUseful() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewLike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.addReviewLike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.getReviewById(reviewId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.useful").value(1));
    }

    @Test
    void reviewMustExistOnAddLike() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.addReviewLike(999, userId).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("review with id 999 not found"));
    }

    @Test
    void userMustExistOnAddLike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewLike(reviewId, 999).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("user with id 999 not found"));
    }

    @Test
    void addDislike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewDislike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.getReviewById(reviewId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.useful").value(-1));
    }

    @Test
    void repeatedAddDislikeBySameUserDoesNotDecreaseUseful() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewDislike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.addReviewDislike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.getReviewById(reviewId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.useful").value(-1));
    }

    @Test
    void reviewMustExistOnAddDislike() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.addReviewDislike(999, userId).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("review with id 999 not found"));
    }

    @Test
    void userMustExistOnAddDislike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewDislike(reviewId, 999).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("user with id 999 not found"));
    }

    @Test
    void deleteLike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewLike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.deleteReviewLike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.getReviewById(reviewId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.useful").value(0));
    }


    @Test
    void reviewMustExistOnDeleteLike() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.deleteReviewLike(999, userId).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("review with id 999 not found"));
    }

    @Test
    void userMustExistOnDeleteLike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.deleteReviewLike(reviewId, 999).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("user with id 999 not found"));
    }

    @Test
    void deleteDislike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.addReviewDislike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.deleteReviewDislike(reviewId, userId).andExpect(status().isOk());
        filmorateApi.getReviewById(reviewId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.useful").value(0));
    }


    @Test
    void reviewMustExistOnDeleteDislike() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.deleteReviewDislike(999, userId).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("review with id 999 not found"));
    }

    @Test
    void userMustExistOnDeleteDislike() throws Exception {
        int filmId = filmorateApi.createAndGetId(new FilmBuilder().build());
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());
        int reviewId = filmorateApi.createAndGetId(new ReviewBuilder().filmId(filmId).userId(userId).build());

        filmorateApi.deleteReviewDislike(reviewId, 999).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("user with id 999 not found"));
    }
}
