package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.FilmorateJdbcConfig;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.friend_requests.FriendRequestDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.testdata.UserBuilder;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        GenreDbStorage.class,
        MpaDbStorage.class,
        UserDbStorage.class,
        FriendRequestDbStorage.class,
        FilmorateJdbcConfig.class,
        DirectorDbStorage.class,
})
public class UserStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void getUserById() {
        User userInput = userStorage.create(new UserBuilder().build());

        User user = userStorage.getById(userInput.getId());

        Assertions.assertEquals(userInput.getId(), user.getId());
        Assertions.assertEquals(userInput.getEmail(), user.getEmail());
        Assertions.assertEquals(userInput.getLogin(), user.getLogin());
        Assertions.assertEquals(userInput.getName(), user.getName());
        Assertions.assertEquals(userInput.getBirthday(), user.getBirthday());
    }

    @Test
    void getAllUsers() {
        User userInput = userStorage.create(new UserBuilder().build());
        List<User> usersInput = List.of(
                userInput,
                userStorage.create(new UserBuilder().build()),
                userStorage.create(new UserBuilder().build())
        );

        Collection<User> users = userStorage.getAll();

        Assertions.assertTrue(users.size() >= usersInput.size());
        Assertions.assertTrue(users.containsAll(usersInput));
        User user = users.stream().filter(f -> f.getId().equals(userInput.getId())).findFirst().get();
        Assertions.assertEquals(userInput.getId(), user.getId());
        Assertions.assertEquals(userInput.getEmail(), user.getEmail());
        Assertions.assertEquals(userInput.getLogin(), user.getLogin());
        Assertions.assertEquals(userInput.getName(), user.getName());
        Assertions.assertEquals(userInput.getBirthday(), user.getBirthday());
    }

    @Test
    void userCreate() {
        User userInput = userStorage.create(new UserBuilder().build());

        User user = userStorage.create(userInput);

        Assertions.assertTrue(user.getId() > 0);
        Assertions.assertEquals(userInput.getEmail(), user.getEmail());
        Assertions.assertEquals(userInput.getLogin(), user.getLogin());
        Assertions.assertEquals(userInput.getName(), user.getName());
        Assertions.assertEquals(userInput.getBirthday(), user.getBirthday());
    }

    @Test
    void userUpdate() {
        User userInput = userStorage.create(new UserBuilder().build());
        userInput.setEmail("new@mail.ru");
        userInput.setLogin("newLogin");
        userInput.setName("New Name");
        userInput.setBirthday(LocalDate.of(2000, 1, 1));

        User user = userStorage.update(userInput);

        Assertions.assertEquals(userInput.getId(), user.getId());
        Assertions.assertEquals(userInput.getEmail(), user.getEmail());
        Assertions.assertEquals(userInput.getLogin(), user.getLogin());
        Assertions.assertEquals(userInput.getName(), user.getName());
        Assertions.assertEquals(userInput.getBirthday(), user.getBirthday());
    }

    @Test
    void userDelete() {
        User userInput = userStorage.create(new UserBuilder().build());

        int userId = userInput.getId();

        userStorage.delete(userId);

        Assertions.assertThrows(NotFoundException.class, () -> userStorage.getById(userId));
    }

    @Test
    void addFriend() {
        User user1 = userStorage.create(new UserBuilder().build());
        User user2 = userStorage.create(new UserBuilder().build());

        userStorage.addFriend(user1.getId(), user2.getId());

        Collection<User> user1Friends = userStorage.getFriends(user1.getId());
        Assertions.assertEquals(1, user1Friends.size());
        Assertions.assertTrue(user1Friends.contains(user2));
        Assertions.assertTrue(userStorage.getFriends(user2.getId()).isEmpty());
    }

    @Test
    void addFriendMutual() {
        User user1 = userStorage.create(new UserBuilder().build());
        User user2 = userStorage.create(new UserBuilder().build());

        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user2.getId(), user1.getId());

        Collection<User> user1Friends = userStorage.getFriends(user1.getId());
        Collection<User> user2Friends = userStorage.getFriends(user2.getId());
        Assertions.assertEquals(1, user1Friends.size());
        Assertions.assertTrue(user1Friends.contains(user2));
        Assertions.assertEquals(1, user2Friends.size());
        Assertions.assertTrue(user2Friends.contains(user1));
    }

    @Test
    void deleteFriend() {
        User user1 = userStorage.create(new UserBuilder().build());
        User user2 = userStorage.create(new UserBuilder().build());
        userStorage.addFriend(user1.getId(), user2.getId());

        userStorage.deleteFriend(user1.getId(), user2.getId());

        Assertions.assertTrue(userStorage.getFriends(user1.getId()).isEmpty());
    }

    @Test
    void deleteAcceptedFriend() {
        User user1 = userStorage.create(new UserBuilder().build());
        User user2 = userStorage.create(new UserBuilder().build());
        userStorage.addFriend(user1.getId(), user2.getId());
        userStorage.addFriend(user2.getId(), user1.getId());

        userStorage.deleteFriend(user1.getId(), user2.getId());

        Assertions.assertTrue(userStorage.getFriends(user1.getId()).isEmpty());
        Collection<User> user2Friends = userStorage.getFriends(user2.getId());
        Assertions.assertEquals(1, user2Friends.size());
        Assertions.assertTrue(user2Friends.contains(user1));
    }

    @Test
    void getFriends() {
        User user = userStorage.create(new UserBuilder().build());
        List<User> friendsInput = List.of(
                userStorage.create(new UserBuilder().build()),
                userStorage.create(new UserBuilder().build()),
                userStorage.create(new UserBuilder().build())
        );
        friendsInput.forEach(f -> userStorage.addFriend(user.getId(), f.getId()));

        Collection<User> friends = userStorage.getFriends(user.getId());

        Assertions.assertEquals(friendsInput.size(), friends.size());
        Assertions.assertTrue(friends.containsAll(friendsInput));
    }

    @Test
    void getFriendsCommon() {
        User user1 = userStorage.create(new UserBuilder().build());
        User user2 = userStorage.create(new UserBuilder().build());
        User friend1 = userStorage.create(new UserBuilder().build());
        User friend2 = userStorage.create(new UserBuilder().build());
        User friend3 = userStorage.create(new UserBuilder().build());
        User friend4 = userStorage.create(new UserBuilder().build());
        User friend5 = userStorage.create(new UserBuilder().build());

        userStorage.addFriend(user1.getId(), friend1.getId());
        userStorage.addFriend(user1.getId(), friend2.getId());
        userStorage.addFriend(user1.getId(), friend3.getId());
        userStorage.addFriend(user1.getId(), friend4.getId());
        userStorage.addFriend(friend3.getId(), user1.getId());
        userStorage.addFriend(friend4.getId(), user1.getId());

        userStorage.addFriend(user2.getId(), friend2.getId());
        userStorage.addFriend(user2.getId(), friend3.getId());
        userStorage.addFriend(user2.getId(), friend4.getId());
        userStorage.addFriend(user2.getId(), friend5.getId());
        userStorage.addFriend(friend5.getId(), user2.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());
        Assertions.assertEquals(3, commonFriends.size());
        Assertions.assertTrue(commonFriends.containsAll(List.of(friend2, friend3, friend4)));
    }
}
