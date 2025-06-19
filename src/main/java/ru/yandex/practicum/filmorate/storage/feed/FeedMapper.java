package ru.yandex.practicum.filmorate.storage.feed;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedMapper implements RowMapper<FeedEvent> {

    @Getter
    private static final FeedMapper instance = new FeedMapper();

    private FeedMapper() {
    }

    @Override
    public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeedEvent event = new FeedEvent();
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getInt("user_id"));
        event.setEventType(FeedEvent.EventType.valueOf(rs.getString("event_type")));
        event.setOperation(FeedEvent.Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getInt("entity_id"));
        return event;
    }
}
