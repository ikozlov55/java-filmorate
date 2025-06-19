package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert feedJdbcInsert;

    @Override
    public Collection<FeedEvent> getUserFeed(int userId) {
        String sql = "SELECT event_id, timestamp, user_id, event_type, operation, entity_id " +
                "FROM user_feeds WHERE user_id = ?";

        return jdbcTemplate.query(sql, FeedMapper.getInstance(), userId);
    }

    @Override
    public void addEvent(FeedEvent event) {
        Map<String, Object> args = new HashMap<>();
        args.put("timestamp", event.getTimestamp());
        args.put("user_id", event.getUserId());
        args.put("event_type", event.getEventType().toString());
        args.put("operation", event.getOperation().toString());
        args.put("entity_id", event.getEntityId());

        feedJdbcInsert.execute(args);
    }
}
