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
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
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
    private final DirectorStorage directorStorage;

    /*
    GROUP_CONCAT — это функция, которая объединяет значения из нескольких строк в одно строковое значение,
    разделяя их указанным разделителем. В данном случае она используется для объединения
    идентификаторов жанров (fg.genre_id) в одну строку, разделённую запятой.
COALESCE — это функция, которая возвращает первое ненулевое значение из списка своих аргументов.
 Если результат GROUP_CONCAT окажется пустым (например, если для фильма нет жанров),
 COALESCE вернёт пустую строку ''.
     */
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
                       COALESCE(group_concat(g.name separator ','), '') AS genres_names,
                       COALESCE(group_concat(fd.director_id separator ','), '') AS directors_ids,
                       COALESCE(group_concat(d.name separator ','), '') AS directors_names,
                  FROM films f
                  JOIN mpa m ON f.mpa_id = m.id
             LEFT JOIN users_films_likes ufl ON f.id = ufl.film_id
             LEFT JOIN films_genres fg ON f.id = fg.film_id
             LEFT JOIN genres g ON fg.genre_id = g.id
             LEFT JOIN films_directors fd ON f.id = fd.film_id
             LEFT JOIN directors d ON fd.director_id = d.id
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
    @Transactional//либо все операции будут успешно выполнены, либо ни одна из них не будет применена в случае ошибки.
    public Film create(Film film) {
        mpaStorage.checkMpaExists(film.getMpa().getId());
//перебирает все жанры, связанные с фильмом, и проверяет существование каждого жанра в хранилище жанров.
        film.getGenres().forEach(g -> genreStorage.checkGenreExists(g.getId()));
//перебирает всех режиссеров, связанных с фильмом, и проверяет существование каждого режиссера в хранилище directors.
        film.getDirectors().forEach(director -> directorStorage.checkDirectorExists(director.getId()));
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("name", film.getName());
        argsMap.put("description", film.getDescription());
        argsMap.put("release_date", film.getReleaseDate());
        argsMap.put("duration", film.getDuration());
        argsMap.put("mpa_id", film.getMpa().getId());
// выполнение SQL-запроса для вставки данных о фильме в базу данных и получение сгенерированного идентификатора фильма.
        int filmId = filmsJdbcInsert.executeAndReturnKey(argsMap).intValue();
        film.setId(filmId);
        //1.  установка связи между фильмом и его жанрами в базе данных, используя полученный идентификатор фильма.
        setFilmGenres(filmId, film.getGenres());
        //2.  установка связи между фильмом и его режиссерами в базе данных, используя полученный идентификатор фильма.
        setFilmDirectors(filmId, film.getDirectors());
        return getById(filmId);
    }

    @Override
    @Transactional //либо все операции будут успешно выполнены, либо ни одна из них не будет применена в случае ошибки.
    public Film update(Film film) {
        checkFilmExists(film.getId());
        mpaStorage.checkMpaExists(film.getMpa().getId());
        //перебирает все жанры, связанные с фильмом, и проверяет существование каждого жанра в хранилище жанров.
        film.getGenres().forEach(g -> genreStorage.checkGenreExists(g.getId()));
        //перебирает таблицу с режиссерами, связанные с фильмом, и проверяет существование каждого режиссера в хранилище.
        film.getDirectors().forEach(director -> directorStorage.checkDirectorExists(director.getId()));
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
        jdbcTemplate.update("DELETE FROM films_directors WHERE film_id = ?", film.getId());
        setFilmGenres(film.getId(), film.getGenres());
        setFilmDirectors(film.getId(), film.getDirectors());
        return getById(film.getId());
    }

    @Override
    @Transactional
    public void delete(int filmId) {
        checkFilmExists(filmId);
        jdbcTemplate.update("DELETE FROM films_genres WHERE film_id = ?", filmId);
        jdbcTemplate.update("DELETE FROM users_films_likes WHERE film_id = ?", filmId);
        jdbcTemplate.update("DELETE FROM films_directors WHERE film_id = ?", filmId);
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", filmId);
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
        return jdbcTemplate.query(query, FilmMapper.getInstance());
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

    @Override
    public Collection<Film> filmSearch(String searchTitle, boolean isDirectorSearch, boolean isTitleSearch) {
        if (searchTitle == null || searchTitle.isEmpty()) {
            throw new IllegalArgumentException("searchTitle cannot be null or empty");
        }
        if (isDirectorSearch && isTitleSearch) {
            String searchQuery = "%" + searchTitle + "%";
            String newQuery = String.format(SELECT_FILMS_QUERY, "WHERE LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?)", "");
            return jdbcTemplate.query(newQuery, FilmMapper.getInstance(), searchQuery, searchQuery);
        } else if (isTitleSearch) {
            String searchQuery = "%" + searchTitle + "%";
            String query = String.format(SELECT_FILMS_QUERY, "WHERE LOWER(f.name) LIKE LOWER(?)", "");
            return jdbcTemplate.query(query, FilmMapper.getInstance(), searchQuery);
        } else if (isDirectorSearch) {
            String searchByQuery = "%" + searchTitle + "%";
            String byQuery = String.format(SELECT_FILMS_QUERY, "WHERE LOWER(d.name) LIke LOWER(?)", "");
            return jdbcTemplate.query(byQuery, FilmMapper.getInstance(), searchByQuery);
        } else {
            throw new IllegalArgumentException("by can be: director or title");
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

    /* Метод setFilmDirectors связывает фильм с его режиссерами в базе данных.
     Вставляя соответствующие записи в таблицу films_directors.
    */
    private void setFilmDirectors(int filmId, Set<Director> directors) {
        List<Director> directorsList = directors.stream().toList();
        //используется для выполнения пакетного обновления (вставки) данных в таблицу films_directors.
        jdbcTemplate.batchUpdate(
                "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)",//SQL-запрос для вставки данных.
                new BatchPreparedStatementSetter() { //определяет, как будут заполняться параметры SQL-запроса.

                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);//устанавливает идентификатор фильма.
                        //устанавливает идентификатор режиссера из списка режиссеров.
                        ps.setInt(2, directorsList.get(i).getId());
                        System.out.println(ps);
                    }

                    //возвращает размер пакета, то есть количество режиссеров, которые нужно связать с фильмом.
                    @Override
                    public int getBatchSize() {
                        return directors.size();
                    }
                }
        );
    }

    private boolean userFilmLikeExists(int filmId, int userId) {
        String query = "SELECT EXISTS (SELECT 1 FROM users_films_likes WHERE film_id = ? AND user_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, filmId, userId);
        return exists != null && exists;
    }

    /*
    GET /films/director/{directorId}?sortBy=[year,likes]
    Возвращает список фильмов режиссера отсортированных по количеству лайков или году выпуска
    Пример запроса: GET /films/director/1?sortBy=likes
     */
    @Override
    public Collection<Film> getFilmsOfDirectors(int directorId, String sortBy) {
        directorStorage.checkDirectorExists(directorId);

        String query;
        if (sortBy.equalsIgnoreCase("year")) {
            query = String.format(
                    SELECT_FILMS_QUERY,
                    "WHERE fd.director_id = ?",
                    "ORDER BY f.release_date");
        } else if (sortBy.equalsIgnoreCase("likes")) {
            query = String.format(SELECT_FILMS_QUERY, "WHERE fd.director_id = ?", "ORDER BY likes desc ");
        } else {
            throw new ValidationException("Error parameter sort film");
        }
        return jdbcTemplate.query(query, FilmMapper.getInstance(), directorId);
    }
}
