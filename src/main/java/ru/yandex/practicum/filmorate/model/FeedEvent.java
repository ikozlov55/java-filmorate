package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedEvent {
    private Integer eventId;
    @NotNull
    private Integer userId;
    private EventType eventType;
    private Operation operation;
    private Integer entityId;
    private Long timestamp;

    public FeedEvent(Integer userId, EventType eventType, Operation operation, Integer entityId) {
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
        this.timestamp = System.currentTimeMillis();
    }

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        REMOVE, ADD, UPDATE
    }
}
