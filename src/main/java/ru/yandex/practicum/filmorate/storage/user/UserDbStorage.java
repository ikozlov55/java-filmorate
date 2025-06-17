package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
        String queryUserEachLike = """
                    SELECT u2.user_id
                    FROM users_films_likes u1
                    JOIN users_films_likes u2 ON u1.film_id = u2.film_id
                    WHERE u1.user_id = ?
                    AND u2.user_id != ?
                    GROUP BY u2.user_id
                    ORDER BY COUNT(u1.film_id) DESC
                    LIMIT 1
                """;
        List<Integer> userIdEachLikesFilms = jdbcTemplate.queryForList(queryUserEachLike, Integer.class, userId, userId);

        String queryForUserLikeFilm = """
                    SELECT DISTINCT ufl1.film_id
                    FROM users_films_likes ufl1
                    WHERE ufl1.user_id = ?
                    AND NOT EXISTS (
                        SELECT 1
                        FROM users_films_likes ufl2
                        WHERE ufl2.user_id = ? AND ufl2.film_id = ufl1.film_id
                    )
                """;
        List<Integer> filmIdUserNotLike = jdbcTemplate.queryForList(queryForUserLikeFilm, Integer.class, userIdEachLikesFilms.get(0), userId);

        String queryForRecommendedFilms = """
                    SELECT
                        f.id,
                        f.name,
                        f.description,
                        f.release_date,
                        f.duration,
                        f.mpa_id,
                        m.name AS mpa_name,
                        COUNT(ufl.film_id) AS likes,
                        COALESCE(LISTAGG(DISTINCT fg.genre_id, ','), '') AS genres_ids,
                        COALESCE(LISTAGG(DISTINCT g.name, ','), '') AS genres_names
                    FROM
                        films f
                    JOIN
                        mpa m ON f.mpa_id = m.id
                    LEFT JOIN
                        users_films_likes ufl ON f.id = ufl.film_id
                    LEFT JOIN
                        films_genres fg ON f.id = fg.film_id
                    LEFT JOIN
                        genres g ON fg.genre_id = g.id
                    WHERE
                        f.id IN (:filmIds)
                    GROUP BY
                        f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                    ORDER BY
                        likes DESC
                """;

        SqlParameterSource params = new MapSqlParameterSource("filmIds", filmIdUserNotLike);

        return namedParameterJdbcTemplate.query(queryForRecommendedFilms, params, FilmMapper.getInstance());
    }
}
