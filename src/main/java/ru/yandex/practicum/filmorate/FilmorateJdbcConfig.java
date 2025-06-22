package ru.yandex.practicum.filmorate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

@Configuration
public class FilmorateJdbcConfig {

    @Bean
    public SimpleJdbcInsert filmsJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("films")
                .usingGeneratedKeyColumns("id");
    }

    @Bean
    public SimpleJdbcInsert directorsJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("directors")
                .usingGeneratedKeyColumns("id");
    }

    @Bean
    public SimpleJdbcInsert filmsLikesJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("users_films_likes");
    }

    @Bean
    public SimpleJdbcInsert usersJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Bean
    public SimpleJdbcInsert friendsRequestsJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("users_friends_requests");
    }

    @Bean
    public SimpleJdbcInsert reviewsJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate).withTableName("reviews")
                .usingGeneratedKeyColumns("id");
    }

    @Bean
    public SimpleJdbcInsert feedJdbcInsert(JdbcTemplate jdbcTemplate) {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("user_feeds")
                .usingGeneratedKeyColumns("event_id");
    }
}
