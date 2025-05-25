package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friend_requests.FriendRequestStatus;
import ru.yandex.practicum.filmorate.storage.friend_requests.FriendRequestStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendRequestStorage friendRequestStorage;

    private static final String SELECT_FILMS_QUERY = """
            SELECT id,
                   email,
                   login,
                   name,
                   birthday
              FROM users
                   %s;
            """;

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query(String.format(SELECT_FILMS_QUERY, ""), new UserMapper());
    }

    @Override
    public User getById(int id) {
        checkUserExists(id);
        return jdbcTemplate.queryForObject(String.format(SELECT_FILMS_QUERY, "WHERE id = ?"), new UserMapper(), id);
    }

    @Override
    public User create(User user) {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("email", user.getEmail());
        argsMap.put("login", user.getLogin());
        argsMap.put("name", user.getName());
        argsMap.put("birthday", user.getBirthday());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        int userId = simpleJdbcInsert.executeAndReturnKey(argsMap).intValue();
        return getById(userId);
    }

    @Override
    public User update(User user) {
        checkUserExists(user.getId());
        jdbcTemplate.update("""
                UPDATE users
                   SET email = ?,
                       login = ?,
                       name = ?,
                       birthday = ?
                 WHERE id = ?;
                """, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return getById(user.getId());
    }

    @Override
    @Transactional
    public User delete(User user) {
        checkUserExists(user.getId());
        jdbcTemplate.update("DELETE FROM users_films_likes WHERE user_id = ?;", user.getId());
        jdbcTemplate.update("DELETE FROM users_friends_requests WHERE user_id = ?;", user.getId());
        jdbcTemplate.update("DELETE FROM users WHERE id = ?;", user.getId());
        return user;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        if (friendRequestStorage.get(userId, friendId).isPresent()) {
            return;
        }
        friendRequestStorage.get(friendId, userId).ifPresentOrElse(
                s -> {
                    if (s == FriendRequestStatus.UNAPPROVED) {
                        friendRequestStorage.update(friendId, userId, FriendRequestStatus.APPROVED);
                    }
                },
                () -> friendRequestStorage.create(userId, friendId, FriendRequestStatus.UNAPPROVED)
        );
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);
        friendRequestStorage.get(userId, friendId).ifPresentOrElse(
                s -> {
                    switch (s) {
                        case UNAPPROVED -> friendRequestStorage.delete(userId, friendId);
                        case APPROVED -> {
                            friendRequestStorage.delete(userId, friendId);
                            friendRequestStorage.create(friendId, userId, FriendRequestStatus.UNAPPROVED);
                        }
                    }
                },
                () -> friendRequestStorage.get(friendId, userId).ifPresent(
                        s -> {
                            if (s == FriendRequestStatus.APPROVED) {
                                friendRequestStorage.update(friendId, userId, FriendRequestStatus.UNAPPROVED);
                            }
                        }
                )
        );
    }

    @Override
    public Collection<User> getFriends(int userId) {
        checkUserExists(userId);
        String query = String.format(SELECT_FILMS_QUERY, """
                WHERE id IN (
                     SELECT friend_id
                       FROM users_friends_requests
                      WHERE user_id = ?
                      UNION
                     SELECT user_id
                       FROM users_friends_requests
                      WHERE friend_id = ?
                        AND status = ?
                     )
                """);
        String status = FriendRequestStatus.APPROVED.name().toLowerCase();
        return jdbcTemplate.query(query, new UserMapper(), userId, userId, status);
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        checkUserExists(userId);
        checkUserExists(otherId);
        String query = String.format(SELECT_FILMS_QUERY, """
                WHERE id IN (
                     (SELECT friend_id
                       FROM users_friends_requests
                      WHERE user_id = ?
                      UNION
                     SELECT user_id
                       FROM users_friends_requests
                      WHERE friend_id = ?
                        AND status = ?)
                  INTERSECT
                     (SELECT friend_id
                       FROM users_friends_requests
                      WHERE user_id = ?
                      UNION
                     SELECT user_id
                       FROM users_friends_requests
                      WHERE friend_id = ?
                        AND status = ?)
                     )
                """);
        String status = FriendRequestStatus.APPROVED.name().toLowerCase();
        return jdbcTemplate.query(query, new UserMapper(), userId, userId, status, otherId, otherId, status);
    }

    @Override
    public void checkUserExists(int id) {
        String query = "SELECT EXISTS (SELECT 1 FROM users WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, id);
        if (exists == null || !exists) {
            String reason = String.format("user with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }
}
