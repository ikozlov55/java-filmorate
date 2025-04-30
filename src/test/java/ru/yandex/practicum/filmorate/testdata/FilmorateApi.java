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
import ru.yandex.practicum.filmorate.model.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@TestComponent
@RequiredArgsConstructor
public class FilmorateApi {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;


    private ResultActions getAllUsers() throws Exception {
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

    private ResultActions getAllFilms() throws Exception {
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

}
