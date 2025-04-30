package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.testdata.FilmorateApi;
import ru.yandex.practicum.filmorate.testdata.UserBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.testdata.Matchers.validationError;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FilmorateApi.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private FilmorateApi filmorateApi;

    @Test
    void userCreate() throws Exception {
        User user = new UserBuilder().build();

        filmorateApi.create(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }

    @Test
    void userCreatedWithoutNameHasLoginInstead() throws Exception {
        User user = new UserBuilder().name(null).build();
        filmorateApi.create(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getLogin()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }


    @Test
    void userUpdate() throws Exception {
        User user = new UserBuilder().build();
        int userId = filmorateApi.createAndGetId(user);
        user.setId(userId);
        user.setEmail("new@mail.ru");
        user.setLogin("newLogin");
        user.setName("New Name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        filmorateApi.update(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }

    @Test
    void userUpdatedWithoutNameHasLoginInstead() throws Exception {
        User user = new UserBuilder().build();
        int userId = filmorateApi.createAndGetId(user);
        user.setId(userId);
        user.setEmail("new@mail.ru");
        user.setLogin("newLogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(2000, 1, 1));

        filmorateApi.update(user).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getLogin()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }

    @Test
    void idIsRequiredOnUserUpdate() throws Exception {
        User user = new UserBuilder().build();

        filmorateApi.update(user).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("id field is required"));
    }

    @Test
    void idMustExistOnUserUpdate() throws Exception {
        User user = new UserBuilder().id(99).build();

        filmorateApi.update(user).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("user with id 99 not found"));
    }

    @Test
    void emailIsRequired() throws Exception {
        User user = new UserBuilder().email(null).build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("email", "must not be blank"));
    }

    @Test
    void emailCannotBeEmpty() throws Exception {
        User user = new UserBuilder().email("").build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("email", "must not be blank"));
    }


    @ParameterizedTest
    @ValueSource(strings = {"zzz", "mail.ru", "@mail.ru"})
    void emailMustBeWellFormed(String email) throws Exception {
        User user = new UserBuilder().email(email).build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("email", "must be a well-formed email address"));
    }

    @Test
    void loginIsRequired() throws Exception {
        User user = new UserBuilder().login(null).build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("login", "must not be null"));
    }

    @Test
    void loginCannotBeEmpty() throws Exception {
        User user = new UserBuilder().login("").build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("login", "must match \"\\S+\""));
    }

    @Test
    void loginCannotContainSpaces() throws Exception {
        User user = new UserBuilder().login("lo gin").build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("login", "must match \"\\S+\""));
    }

    @Test
    void birthdayIsRequired() throws Exception {
        User user = new UserBuilder().birthday(null).build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("birthday", "must not be null"));
    }


    @Test
    void birthdayMustBeInThePast() throws Exception {
        User user = new UserBuilder().birthday(LocalDate.now().plusYears(1)).build();

        filmorateApi.create(user).andExpect(status().isBadRequest())
                .andExpect(validationError("birthday", "must be a past date"));
    }

    @Test
    void getUserById() throws Exception {
        User user = new UserBuilder().build();
        int userId = filmorateApi.createAndGetId(user);

        filmorateApi.getUserById(userId).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.login").value(user.getLogin()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
    }

    @Test
    void idMustExistOnGetUserById() throws Exception {
        filmorateApi.getUserById(999).andExpect(status().isNotFound());
    }

    @Test
    void addFriend() throws Exception {
        int userId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int userId2 = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.addFriend(userId1, userId2).andExpect(status().isOk());
        filmorateApi.getFriends(userId1).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(userId2));
        filmorateApi.getFriends(userId2).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(userId1));
    }

    @Test
    void repeatedAddFriendDoesNotDuplicate() throws Exception {
        int userId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int userId2 = filmorateApi.createAndGetId(new UserBuilder().build());
        filmorateApi.addFriend(userId1, userId2);
        filmorateApi.addFriend(userId2, userId1);
        filmorateApi.addFriend(userId1, userId2);

        filmorateApi.getFriends(userId1).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(userId2));
        filmorateApi.getFriends(userId2).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(userId1));
    }

    @Test
    void userIdMustExistOnAddFriend() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.addFriend(userId, 999).andExpect(status().isNotFound());
    }

    @Test
    void friendIdMustExistOnAddFriend() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.addFriend(999, userId).andExpect(status().isNotFound());
    }

    @Test
    void deleteFriend() throws Exception {
        int userId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int userId2 = filmorateApi.createAndGetId(new UserBuilder().build());
        filmorateApi.addFriend(userId1, userId2);

        filmorateApi.deleteFriend(userId1, userId2).andExpect(status().isOk());
        filmorateApi.getFriends(userId1).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        filmorateApi.getFriends(userId2).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteFriendWhenNotFriendsHasNoError() throws Exception {
        int userId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int userId2 = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.deleteFriend(userId1, userId2).andExpect(status().isOk());
    }

    @Test
    void userIdMustExistOnDeleteFriend() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.deleteFriend(userId, 999).andExpect(status().isNotFound());
    }

    @Test
    void friendIdMustExistOnDeleteFriend() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.deleteFriend(999, userId).andExpect(status().isNotFound());
    }

    @Test
    void getCommonFriends() throws Exception {
        int userId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int userId2 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId2 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId3 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId4 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId5 = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.addFriend(userId1, friendId1);
        filmorateApi.addFriend(userId1, friendId2);
        filmorateApi.addFriend(userId1, friendId3);
        filmorateApi.addFriend(userId1, friendId4);

        filmorateApi.addFriend(userId2, friendId3);
        filmorateApi.addFriend(userId2, friendId4);
        filmorateApi.addFriend(userId2, friendId5);

        filmorateApi.getCommonFriends(userId1, userId2).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(friendId3))
                .andExpect(jsonPath("$.[1].id").value(friendId4));
        filmorateApi.getCommonFriends(userId2, userId1).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$.[0].id").value(friendId3))
                .andExpect(jsonPath("$.[1].id").value(friendId4));
    }

    @Test
    void getCommonFriendsWhenEmpty() throws Exception {
        int userId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int userId2 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId1 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId2 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId3 = filmorateApi.createAndGetId(new UserBuilder().build());
        int friendId4 = filmorateApi.createAndGetId(new UserBuilder().build());
        filmorateApi.addFriend(userId1, friendId1);
        filmorateApi.addFriend(userId1, friendId2);
        filmorateApi.addFriend(userId2, friendId3);
        filmorateApi.addFriend(userId2, friendId4);

        filmorateApi.getCommonFriends(userId1, userId2).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        filmorateApi.getCommonFriends(userId2, userId1).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void userIdMustExistOnGetCommonFriends() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.getCommonFriends(userId, 999).andExpect(status().isNotFound());
    }

    @Test
    void otherIdMustExistOnGetCommonFriends() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.getCommonFriends(999, userId).andExpect(status().isNotFound());
    }

    @Test
    void getFriendsIsEmptyWhenNoFriends() throws Exception {
        int userId = filmorateApi.createAndGetId(new UserBuilder().build());

        filmorateApi.getFriends(userId).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
