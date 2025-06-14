package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmMapper;
import ru.yandex.practicum.filmorate.storage.friend_requests.FriendRequestStatus;
import ru.yandex.practicum.filmorate.storage.friend_requests.FriendRequestStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendRequestStorage friendRequestStorage;
    private final SimpleJdbcInsert usersJdbcInsert;

    private static final String SELECT_FILMS_QUERY = """
            SELECT id,
                   email,
                   login,
                   name,
                   birthday
              FROM users
                   %s
            """;

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query(String.format(SELECT_FILMS_QUERY, ""), UserMapper.getInstance());
    }

    @Override
    public User getById(int id) {
        checkUserExists(id);
        return jdbcTemplate.queryForObject(String.format(SELECT_FILMS_QUERY, "WHERE id = ?"), UserMapper.getInstance(), id);
    }

    @Override
    public User create(User user) {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("email", user.getEmail());
        argsMap.put("login", user.getLogin());
        argsMap.put("name", user.getName());
        argsMap.put("birthday", user.getBirthday());

        int userId = usersJdbcInsert.executeAndReturnKey(argsMap).intValue();
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
                 WHERE id = ?
                """, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return getById(user.getId());
    }

    @Override
    @Transactional
    public User delete(User user) {
        checkUserExists(user.getId());
        jdbcTemplate.update("DELETE FROM users_films_likes WHERE user_id = ?", user.getId());
        jdbcTemplate.update("DELETE FROM users_friends_requests WHERE user_id = ?", user.getId());
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());
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
        return jdbcTemplate.query(query, UserMapper.getInstance(), userId, userId, status);
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
        return jdbcTemplate.query(query, UserMapper.getInstance(), userId, userId, status, otherId, otherId, status);
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

    @Override
    public Collection<Film> getRecommendations(int userId) {
        String query = """
                    SELECT f.id,
                           f.name,
                           f.description,
                           f.release_date,
                           f.duration,
                           f.mpa_id,
                           m.name AS mpa_name,
                           COUNT(ufl.film_id) AS likes,
                           COALESCE(group_concat(fg.genre_id separator ','), '') AS genres_ids,
                           COALESCE(group_concat(g.name separator ','), '') AS genres_names
                      FROM films f
                      JOIN mpa m ON f.mpa_id = m.id
                 LEFT JOIN users_films_likes ufl ON f.id = ufl.film_id
                 LEFT JOIN films_genres fg ON f.id = fg.film_id
                 LEFT JOIN genres g ON fg.genre_id = g.id
                           %s
                     GROUP BY f.id
                           %s
                """;
        Collection<Film> userFilmsLikes = jdbcTemplate.query(String.format(query, "where ufl.user_id = ?", ""), FilmMapper.getInstance(), userId);
        Collection<User> users = getAll();
        users.remove(getById(userId));
        Map<User, Integer> usersLikes = new HashMap<>();
        for (Film film : userFilmsLikes) {
            for (User u : users) {
                Collection<Film> films = jdbcTemplate.query(String.format(query, "where ufl.user_id = ?", ""), FilmMapper.getInstance(), u.getId());
                if (films.contains(film)) {
                    if (usersLikes.containsKey(u)) {
                        usersLikes.put(u, usersLikes.get(u) + 1);
                        continue;
                    }
                    usersLikes.put(u, 1);
                }
            }
        }
        for (Map.Entry<User, Integer> entry : usersLikes.entrySet()) {
            User key = entry.getKey();
            Integer value = entry.getValue();
            log.info("Key: " + key + ", Value: " + value);
        }
        return userFilmsLikes;
    }
}
