package ru.yandex.practicum.filmorate.storage.feed;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class FeedMapper implements RowMapper<FeedEvent> {

    @Getter
    private static final FeedMapper instance = new FeedMapper();

    private FeedMapper() {
    }

    @Override
    public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FeedEvent(rs.getInt("event_id"),
                rs.getInt("user_id"),
                FeedEvent.EventType.valueOf(rs.getString("event_type")),
                FeedEvent.Operation.valueOf(rs.getString("operation")),
                rs.getInt("entity_id"),
                rs.getTimestamp("created_at").toInstant()
        );
    }
}
