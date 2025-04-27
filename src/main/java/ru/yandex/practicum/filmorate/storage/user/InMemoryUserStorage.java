package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private static int nextEntityId = 1;
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> usersFriends = new HashMap<>();

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public User getById(int id) {
        checkEntityExists(id);
        return users.get(id);
    }

    @Override
    public User create(User user) {
        user.setId(nextEntityId);
        users.put(user.getId(), user);
        nextEntityId++;
        return user;
    }

    @Override
    public User update(User user) {
        checkEntityExists(user.getId());
        User oldUser = users.get(user.getId());
        oldUser.setEmail(user.getEmail());
        oldUser.setLogin(user.getLogin());
        oldUser.setName(user.getName());
        oldUser.setBirthday(user.getBirthday());
        return oldUser;
    }

    @Override
    public User delete(User user) {
        checkEntityExists(user.getId());
        return users.remove(user.getId());
    }

    @Override
    public void addFriend(int userId, int friendId) {
        checkEntityExists(userId);
        checkEntityExists(friendId);
        if (!usersFriends.containsKey(userId)) {
            usersFriends.put(userId, new HashSet<>());
        }
        if (!usersFriends.containsKey(friendId)) {
            usersFriends.put(friendId, new HashSet<>());
        }
        usersFriends.get(userId).add(friendId);
        usersFriends.get(friendId).add(userId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        checkEntityExists(userId);
        checkEntityExists(friendId);
        if (usersFriends.containsKey(userId)) {
            usersFriends.get(userId).remove(friendId);
        }
        if (usersFriends.containsKey(friendId)) {
            usersFriends.get(friendId).remove(userId);
        }
    }

    @Override
    public Collection<User> getFriends(int userId) {
        checkEntityExists(userId);
        if (!usersFriends.containsKey(userId)) {
            return Set.of();
        }
        return usersFriends.get(userId).stream().map(this::getById).toList();
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        checkEntityExists(userId);
        checkEntityExists(otherId);
        if (!usersFriends.containsKey(userId) || !usersFriends.containsKey(otherId)) {
            return Set.of();
        }
        Set<Integer> commonFriendsIds = usersFriends.get(userId);
        commonFriendsIds.retainAll(usersFriends.get(otherId));
        return commonFriendsIds.stream().map(this::getById).toList();
    }

    private void checkEntityExists(int id) {
        if (!users.containsKey(id)) {
            String reason = String.format("user with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }
}
