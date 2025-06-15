package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;


@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert filmsJdbcInsert;
    private final SimpleJdbcInsert filmsLikesJdbcInsert;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private static final String SELECT_FILMS_QUERY = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa_id,
                       m.name AS mpa_name,
                       COUNT(ufl.film_id) AS likes,
                       COALESCE(group_concat(fg.genre_id separator ','), '') AS genres_ids,
                       COALESCE(group_concat(g.name separator ','), '') AS genres_names
                  FROM films f
                  JOIN mpa m ON f.mpa_id = m.id
             LEFT JOIN users_films_likes ufl ON f.id = ufl.film_id
             LEFT JOIN films_genres fg ON f.id = fg.film_id
             LEFT JOIN genres g ON fg.genre_id = g.id
                       %s
                 GROUP BY f.id
                       %s
            """;

    @Override
    public Collection<Film> getAll() {
        String query = String.format(SELECT_FILMS_QUERY, "", "");
        return jdbcTemplate.query(query, FilmMapper.getInstance());
    }

    @Override
    public Film getById(int id) {
        checkFilmExists(id);
        String query = String.format(SELECT_FILMS_QUERY, "WHERE f.id = ?", "");
        return jdbcTemplate.queryForObject(query, FilmMapper.getInstance(), id);
    }

    @Override
    @Transactional
    public Film create(Film film) {
        mpaStorage.checkMpaExists(film.getMpa().getId());
        film.getGenres().forEach(g -> genreStorage.checkGenreExists(g.getId()));
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("name", film.getName());
        argsMap.put("description", film.getDescription());
        argsMap.put("release_date", film.getReleaseDate());
        argsMap.put("duration", film.getDuration());
        argsMap.put("mpa_id", film.getMpa().getId());

        int filmId = filmsJdbcInsert.executeAndReturnKey(argsMap).intValue();
        setFilmGenres(filmId, film.getGenres());
        return getById(filmId);
    }

    @Override
    @Transactional
    public Film update(Film film) {
        checkFilmExists(film.getId());
        mpaStorage.checkMpaExists(film.getMpa().getId());
        film.getGenres().forEach(g -> genreStorage.checkGenreExists(g.getId()));
        jdbcTemplate.update("""
                          UPDATE films
                          SET name = ?,
                              description = ?,
                              release_date = ?,
                              duration = ?,
                              mpa_id = ?
                        WHERE id = ?
                        """, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        jdbcTemplate.update("DELETE FROM films_genres WHERE film_id = ?", film.getId());
        setFilmGenres(film.getId(), film.getGenres());
        return getById(film.getId());
    }

    @Override
    @Transactional
    public Film delete(Film film) {
        checkFilmExists(film.getId());
        jdbcTemplate.update("DELETE FROM films_genres WHERE film_id = ?", film.getId());
        jdbcTemplate.update("DELETE FROM users_films_likes WHERE film_id = ?", film.getId());
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", film.getId());
        return film;
    }

    @Override
    public void addLike(int filmId, int userId) {
        checkFilmExists(filmId);
        userStorage.checkUserExists(userId);
        if (userFilmLikeExists(filmId, userId)) {
            return;
        }
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("film_id", filmId);
        argsMap.put("user_id", userId);
        filmsLikesJdbcInsert.execute(argsMap);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        checkFilmExists(filmId);
        userStorage.checkUserExists(userId);
        jdbcTemplate.update("""
                DELETE
                  FROM users_films_likes
                 WHERE film_id = ?
                   AND user_id = ?
                """, filmId, userId);
    }


    /*
        GET /films/popular?count={limit}&genreId={genreId}&year={year}
        Возвращает список топ-N фильмов по количеству лайков указанного жанра за нужный год.
    */
    @Override
    public Collection<Film> filmsPopular(Integer genreId, String year, Integer count) {
        //String requirement = "";
        String requirementOrder = String.format(
                "ORDER BY likes DESC LIMIT %d ", count);

        String requirement = String.format(
                "WHERE genre_id = %d AND EXTRACT(YEAR FROM release_date) = '%s' ",
                genreId, year);


        if (year == null && genreId != null) {
            requirement = String.format("WHERE genre_id = %d ", genreId);
        }

        if (year != null && genreId == null) {
            requirement = String.format("WHERE EXTRACT(YEAR FROM release_date) = '%s' ", year);
        }
        if (year == null && genreId == null) {
            requirement = "";
        }


        String query = String.format(SELECT_FILMS_QUERY, requirement, requirementOrder);
        List<Film> a = jdbcTemplate.query(query, FilmMapper.getInstance());
        return a;
    }

    @Override
    public void checkFilmExists(int id) {
        String query = "SELECT EXISTS (SELECT 1 FROM films WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, id);
        if (exists == null || !exists) {
            String reason = String.format("film with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }

    private void setFilmGenres(int filmId, Set<Genre> genres) {
        List<Genre> genresList = genres.stream().toList();
        jdbcTemplate.batchUpdate(
                "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, genresList.get(i).getId());
                        System.out.println(ps);
                    }

                    @Override
                    public int getBatchSize() {
                        return genres.size();
                    }
                }
        );
    }

    private boolean userFilmLikeExists(int filmId, int userId) {
        String query = "SELECT EXISTS (SELECT 1 FROM users_films_likes WHERE film_id = ? AND user_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, filmId, userId);
        return exists != null && exists;
    }
}
