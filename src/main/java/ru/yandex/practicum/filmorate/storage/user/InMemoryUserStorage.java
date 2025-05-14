package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

enum FriendshipStatus {
    APPROVED, UNAPPROVED
}

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private static int nextEntityId = 1;
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Map<Integer, FriendshipStatus>> usersFriends = new HashMap<>();


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
        users.put(user.getId(), user);
        return user;
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
            usersFriends.put(userId, new HashMap<>());
        }
        if (!usersFriends.containsKey(friendId)) {
            usersFriends.put(friendId, new HashMap<>());
        }

        if (usersFriends.get(friendId).containsKey(userId)) {
            usersFriends.get(userId).put(friendId, FriendshipStatus.APPROVED);
            usersFriends.get(friendId).put(userId, FriendshipStatus.APPROVED);
        }
        if (!usersFriends.get(userId).containsKey(friendId)) {
            usersFriends.get(userId).put(friendId, FriendshipStatus.UNAPPROVED);
        }
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
        return usersFriends.getOrDefault(userId, Map.of())
                .entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.APPROVED)
                .map(Map.Entry::getKey)
                .map(this::getById).toList();
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        checkEntityExists(userId);
        checkEntityExists(otherId);
        Set<Integer> commonFriendsIds = usersFriends.getOrDefault(userId, Map.of())
                .entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.APPROVED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        commonFriendsIds.retainAll(usersFriends.getOrDefault(otherId, Map.of()).entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.APPROVED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));
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
