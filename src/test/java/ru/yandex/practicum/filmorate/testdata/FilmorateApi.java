package ru.yandex.practicum.filmorate.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@TestComponent
@RequiredArgsConstructor
public class FilmorateApi {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;


    public ResultActions getAllUsers() throws Exception {
        return mockMvc.perform(get("/users"));
    }

    public ResultActions getUserById(int userId) throws Exception {
        return mockMvc.perform(get("/users/{userId}", userId));
    }

    public ResultActions create(User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        return mockMvc.perform(post("/users")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public int createAndGetId(User user) throws Exception {
        MvcResult result = create(user).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    public ResultActions update(User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        return mockMvc.perform(put("/users")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions addFriend(int userId, int friendId) throws Exception {
        return mockMvc.perform(put("/users/{userId}/friends/{friendId}", userId, friendId));
    }

    public ResultActions deleteFriend(int userId, int friendId) throws Exception {
        return mockMvc.perform(delete("/users/{userId}/friends/{friendId}", userId, friendId));
    }

    public ResultActions getFriends(int userId) throws Exception {
        return mockMvc.perform(get("/users/{userId}/friends", userId));
    }

    public ResultActions getCommonFriends(int userId, int otherId) throws Exception {
        return mockMvc.perform(get("/users/{userId}/friends/common/{otherId}", userId, otherId));
    }

    public ResultActions getAllFilms() throws Exception {
        return mockMvc.perform(get("/films"));
    }

    public ResultActions getFilmById(int filmId) throws Exception {
        return mockMvc.perform(get("/films/{filmId}", filmId));
    }

    public ResultActions create(Film film) throws Exception {
        String body = objectMapper.writeValueAsString(film);
        return mockMvc.perform(post("/films")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public int createAndGetId(Film film) throws Exception {
        MvcResult result = create(film).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    public ResultActions update(Film film) throws Exception {
        String body = objectMapper.writeValueAsString(film);
        return mockMvc.perform(put("/films")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions addLike(int filmId, int userId) throws Exception {
        return mockMvc.perform(put("/films/{filmId}/like/{userId}", filmId, userId));
    }

    public ResultActions deleteLike(int filmId, int userId) throws Exception {
        return mockMvc.perform(delete("/films/{filmId}/like/{userId}", filmId, userId));
    }

    public ResultActions filmsPopular(Integer count) throws Exception {
        return mockMvc.perform(get("/films/popular?count={count}", count));
    }

    public ResultActions filmsCommon(int userId, int friendId) throws Exception {
        return mockMvc.perform(get("/films/common?userId={userId}&friendId={friendId}", userId, friendId));
    }

    public ResultActions getGenreById(int genreId) throws Exception {
        return mockMvc.perform(get("/genres/{genreId}", genreId));
    }

    public ResultActions getAllGenres() throws Exception {
        return mockMvc.perform(get("/genres"));
    }

    public ResultActions getMpaById(int mpaId) throws Exception {
        return mockMvc.perform(get("/mpa/{mpaId}", mpaId));
    }

    public ResultActions getAllMpa() throws Exception {
        return mockMvc.perform(get("/mpa"));
    }

    public ResultActions getAllReviews(Integer filmId, Integer count) throws Exception {
        return mockMvc.perform(get("/reviews?filmId={filmId}&count={count}", filmId, count));
    }

    public ResultActions getReviewById(int reviewId) throws Exception {
        return mockMvc.perform(get("/reviews/{reviewId}", reviewId));
    }

    public ResultActions create(Review review) throws Exception {
        String body = objectMapper.writeValueAsString(review);
        return mockMvc.perform(post("/reviews")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public int createAndGetId(Review review) throws Exception {
        MvcResult result = create(review).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.reviewId");
    }

    public ResultActions update(Review review) throws Exception {
        String body = objectMapper.writeValueAsString(review);
        return mockMvc.perform(put("/reviews")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    public ResultActions deleteReview(int reviewId) throws Exception {
        return mockMvc.perform(delete("/reviews/{reviewId}", reviewId));
    }

    public ResultActions addReviewLike(int reviewId, int userId) throws Exception {
        return mockMvc.perform(put("/reviews/{reviewId}/like/{userId}", reviewId, userId));
    }

    public ResultActions addReviewDislike(int reviewId, int userId) throws Exception {
        return mockMvc.perform(put("/reviews/{reviewId}/dislike/{userId}", reviewId, userId));
    }

    public ResultActions deleteReviewLike(int reviewId, int userId) throws Exception {
        return mockMvc.perform(delete("/reviews/{reviewId}/like/{userId}", reviewId, userId));
    }

    public ResultActions deleteReviewDislike(int reviewId, int userId) throws Exception {
        return mockMvc.perform(delete("/reviews/{reviewId}/dislike/{userId}", reviewId, userId));
    }
}
