package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class FilmMapper implements RowMapper<Film> {
    @Getter
    private static final FilmMapper instance = new FilmMapper();

    private FilmMapper() {
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setLikes(rs.getInt("likes"));
        String genresIdsStr = rs.getString("genres_ids");
        String genresNamesStr = rs.getString("genres_names");
        List<Integer> genresIds = Arrays.stream(genresIdsStr.split(","))
                .filter(x -> !x.isBlank())
                .map(Integer::parseInt)
                .toList();
        List<String> genresNames = Arrays.stream(genresNamesStr.split(",")).toList();
        Set<Genre> genres = IntStream.range(0, genresIds.size())
                .mapToObj(i -> {
                    Genre genre = new Genre();
                    genre.setId(genresIds.get(i));
                    genre.setName(genresNames.get(i));
                    return genre;
                })
                .collect(Collectors.toSet());
        Set<Genre> sortedGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
        sortedGenres.addAll(genres);
        film.setGenres(sortedGenres);

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);
        return film;
    }

}
