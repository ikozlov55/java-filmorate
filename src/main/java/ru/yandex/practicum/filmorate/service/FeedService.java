package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedDbStorage feedStorage;

    public Collection<FeedEvent> getUserFeed(int userId) {
        return feedStorage.getUserFeed(userId);
    }
}
