package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FeedEvent {
    @NotNull
    private Integer eventId;
    private Integer userId;
    private EventType eventType;
    private Operation operation;
    private Integer entityId;
    private Long timestamp;

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        REMOVE, ADD, UPDATE
    }
}
