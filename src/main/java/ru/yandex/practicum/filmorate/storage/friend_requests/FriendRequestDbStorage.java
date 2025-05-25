package ru.yandex.practicum.filmorate.storage.friend_requests;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class FriendRequestDbStorage implements FriendRequestStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<FriendRequestStatus> get(int userId, int friendId) {
        return jdbcTemplate.queryForStream("""
                        SELECT status
                          FROM users_friends_requests
                         WHERE user_id = ?
                           AND friend_id = ?
                        """, (rs, rowNum) -> rs.getString("status"), userId, friendId)
                .map(String::toUpperCase)
                .map(FriendRequestStatus::valueOf)
                .findFirst();
    }

    @Override
    public void create(int userId, int friendId, FriendRequestStatus status) {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("user_id", userId);
        argsMap.put("friend_id", friendId);
        argsMap.put("status", status.name().toLowerCase());

        new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users_friends_requests")
                .execute(argsMap);
    }

    @Override
    public void update(int userId, int friendId, FriendRequestStatus status) {
        jdbcTemplate.update(""" 
                UPDATE users_friends_requests
                   SET status = ?
                 WHERE user_id = ?
                   AND friend_id = ?
                """, status.name().toLowerCase(), userId, friendId);
    }

    @Override
    public void delete(int userId, int friendId) {
        jdbcTemplate.update("""
                DELETE
                  FROM users_friends_requests
                 WHERE user_id = ?
                   AND friend_id = ?
                """, userId, friendId);
    }

}
