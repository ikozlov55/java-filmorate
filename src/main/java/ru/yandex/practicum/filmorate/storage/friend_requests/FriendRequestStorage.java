package ru.yandex.practicum.filmorate.storage.friend_requests;

import java.util.Optional;

public interface FriendRequestStorage {
    Optional<FriendRequestStatus> get(int userId, int friendId);

    void create(int userId, int friendId, FriendRequestStatus status);

    void update(int userId, int friendId, FriendRequestStatus status);

    void delete(int userId, int friendId);

}
