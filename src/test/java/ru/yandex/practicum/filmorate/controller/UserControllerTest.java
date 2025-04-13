package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.testdata.UserBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.testdata.Matchers.validationError;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userCreate() throws Exception {
        User user = new UserBuilder().build();

        create(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }

    @Test
    void userCreatedWithoutNameHasLoginInstead() throws Exception {
        User user = new UserBuilder().name(null).build();
        create(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getLogin()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }


    @Test
    void userUpdate() throws Exception {
        User user = new UserBuilder().build();
        create(user).andExpect(status().isOk());
        user.setId(1);
        user.setEmail("new@mail.ru");
        user.setLogin("newLogin");
        user.setName("New Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        update(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }

    @Test
    void idIsRequiredOnUserUpdate() throws Exception {
        User user = new UserBuilder().build();

        update(user).andExpect(status().isBadRequest())
                .andExpect(status().reason("id field is required"));
    }

    @Test
    void idMustExistsOnUserUpdate() throws Exception {
        User user = new UserBuilder().id(99).build();

        update(user).andExpect(status().isNotFound())
                .andExpect(status().reason("user with id 99 not found"));
    }

    @Test
    void emailIsRequired() throws Exception {
        User user = new UserBuilder().email(null).build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("email", "must not be blank"));
    }

    @Test
    void emailCannotBeEmpty() throws Exception {
        User user = new UserBuilder().email("").build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("email", "must not be blank"));
    }


    @ParameterizedTest
    @ValueSource(strings = {"zzz", "mail.ru", "@mail.ru"})
    void emailMustBeWellFormed(String email) throws Exception {
        User user = new UserBuilder().email(email).build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("email", "must be a well-formed email address"));
    }

    @Test
    void loginIsRequired() throws Exception {
        User user = new UserBuilder().login(null).build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("login", "must not be null"));
    }

    @Test
    void loginCannotBeEmpty() throws Exception {
        User user = new UserBuilder().login("").build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("login", "must match \"\\S+\""));
    }

    @Test
    void loginCannotContainSpaces() throws Exception {
        User user = new UserBuilder().login("lo gin").build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("login", "must match \"\\S+\""));
    }

    @Test
    void birthdayIsRequired() throws Exception {
        User user = new UserBuilder().birthday(null).build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("birthday", "must not be null"));
    }


    @Test
    void birthdayMustBeInThePast() throws Exception {
        User user = new UserBuilder().birthday(LocalDate.now().plusYears(1)).build();

        create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("birthday", "must be a past date"));
    }


    ResultActions create(User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        return mockMvc.perform(post("/users")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }

    ResultActions update(User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);
        return mockMvc.perform(put("/users")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON));
    }
}
