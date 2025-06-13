package ru.yandex.practicum.filmorate.testdata;

import net.datafaker.Faker;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Locale;

public class ReviewBuilder {
    private Integer id;
    private String content;
    private Boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful;
    Faker faker = new Faker(Locale.of("RU"));

    public ReviewBuilder() {
        content = faker.lorem().sentence(faker.random().nextInt(1, 1000));
        isPositive = faker.bool().bool();
    }

    public ReviewBuilder id(int id) {
        this.id = id;
        return this;
    }

    public ReviewBuilder content(String content) {
        this.content = content;
        return this;
    }

    public ReviewBuilder isPositive(Boolean isPositive) {
        this.isPositive = isPositive;
        return this;
    }

    public ReviewBuilder userId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public ReviewBuilder filmId(Integer filmId) {
        this.filmId = filmId;
        return this;
    }

    public ReviewBuilder useful(Integer useful) {
        this.useful = useful;
        return this;
    }

    public Review build() {
        Review review = new Review();
        review.setId(id);
        review.setContent(content);
        review.setIsPositive(isPositive);
        review.setUserId(userId);
        review.setFilmId(filmId);
        review.setUseful(useful);
        return review;
    }
}
