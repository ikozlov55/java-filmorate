package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;
    private final ReviewStorage reviewStorage;

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id);
    }

    public User create(User user) {
        log.info("User create request received: {}", user);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        User createdUser = userStorage.create(user);
        log.info("User created successfully: {}", createdUser);
        return createdUser;
    }

    public User update(User user) {
        log.info("User update request received {}", user);
        if (user.getId() == null) {
            String reason = "id field is required";
            log.warn("Validation failed: {}", reason);
            throw new ValidationException(reason);
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        User updatedUser = userStorage.update(user);
        log.info("User updated successfully: {}", updatedUser);
        return updatedUser;
    }

    @Transactional
    public void delete(int userId) {
        log.info("User delete request received {}", userId);
        reviewStorage.deleteByUserId(userId);
        userStorage.delete(userId);
        log.info("User deleted successfully: {}", userId);
    }


    public void addFriend(int userId, int friendId) {
        userStorage.addFriend(userId, friendId);
    }


    public void deleteFriend(int userId, int friendId) {
        userStorage.deleteFriend(userId, friendId);
    }


    public Collection<User> getFriends(int userId) {
        return userStorage.getFriends(userId);
    }


    public Collection<User> getCommonFriends(int userId, int otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }
}
