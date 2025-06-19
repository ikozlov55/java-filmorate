package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.Collection;

public interface FeedStorage {
    Collection<FeedEvent> getUserFeed(int userId);

    void addEvent(FeedEvent event);

    FeedEvent createFeedEvent(int userId, FeedEvent.EventType eventType,
                              FeedEvent.Operation operation, int entityId);
}
