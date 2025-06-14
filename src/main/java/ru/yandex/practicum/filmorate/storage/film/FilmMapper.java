package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Director;
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

//1.	Получение строк с идентификаторами и названиями жанров
        String genresIdsStr = rs.getString("genres_ids");
        String genresNamesStr = rs.getString("genres_names");
        String directorsIdsStr = rs.getString("directors_ids");
        String directorsNamesStr = rs.getString("directors_names");


//2. 	Преобразование строки идентификаторов в список целых чисел
           /*
        - Строка genresIdsStr разделяется по запятым на массив подстрок.
        - Из массива удаляются пустые и состоящие только из пробелов элементы.
        - Каждая подстрока преобразуется в целое число (Integer).
        - Результат сохраняется в список genresIds`.
         */
        List<Integer> genresIds = Arrays.stream(genresIdsStr.split(","))
                .filter(x -> !x.isBlank())
                .map(Integer::parseInt)
                .toList();

        List<Integer> directorsIds = Arrays.stream(directorsIdsStr.split(","))
                .filter(x -> !x.isBlank())
                .map(Integer::parseInt)
                .toList();

//3.	Преобразование строки названий в список строк
        List<String> genresNames = Arrays.stream(genresNamesStr.split(",")).toList();
        List<String> directorsNames = Arrays.stream(directorsNamesStr.split(",")).toList();

//4.	Создание набора объектов Genre
        /*
         - Создаётся поток целых чисел от 0 до genresIds.size() - 1.
         - Для каждого индекса i создаётся новый объект Genre,
           которому присваиваются id и name из соответствующих списков.
         - Объекты собираются в набор genres
         */
        Set<Genre> genres = IntStream.range(0, genresIds.size())
                .mapToObj(i -> {
                    Genre genre = new Genre();
                    genre.setId(genresIds.get(i));
                    genre.setName(genresNames.get(i));
                    return genre;
                })
                .collect(Collectors.toSet());

        Set<Director> directors = IntStream.range(0, directorsIds.size())
                .mapToObj(i -> {
                    Director director = new Director();
                    director.setId(directorsIds.get(i));
                    director.setName(directorsNames.get(i));
                    return director;
                })
                .collect(Collectors.toSet());

//5.	Сортировка жанров по идентификатору
        /*
        - Создаётся новый TreeSet, который автоматически сортирует элементы
          по заданному компаратору — в данном случае по id.
        - В TreeSet добавляются все элементы из набора genres
         */
        Set<Genre> sortedGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
        sortedGenres.addAll(genres);
        film.setGenres(sortedGenres);

        Set<Director> sortedDirectors = new TreeSet<>(Comparator.comparing(Director::getId));
        sortedDirectors.addAll(directors);
        film.setDirectors(sortedDirectors);

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);
        return film;
    }
}
