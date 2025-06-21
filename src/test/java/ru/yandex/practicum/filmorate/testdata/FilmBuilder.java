package ru.yandex.practicum.filmorate.testdata;

import net.datafaker.Faker;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class FilmBuilder {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Mpa mpa;
    private Set<Genre> genres;
    Faker faker = new Faker(Locale.of("RU"));

    public FilmBuilder() {
        name = faker.movie().name();
        description = makeDescription();
        releaseDate = faker.timeAndDate().birthday(0, 40);
        duration = faker.random().nextInt(60, 120);
        mpa = new Mpa(faker.random().nextInt(1, 5), null);
        Genre genre = new Genre(faker.random().nextInt(1, 6), null);
        genres = Set.of(genre);
    }

    public FilmBuilder id(int id) {
        this.id = id;
        return this;
    }

    public FilmBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FilmBuilder description(String description) {
        this.description = description;
        return this;
    }

    public FilmBuilder releaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public FilmBuilder duration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public FilmBuilder mpa(Integer mpaId) {
        this.mpa = new Mpa(mpaId, null);
        return this;
    }

    public FilmBuilder genres(int... genresIds) {
        Set<Genre> genres = new HashSet<>();
        for (int genreId : genresIds) {
            Genre genre = new Genre(genreId, null);
            genres.add(genre);
        }
        this.genres = genres;
        return this;
    }

    public Film build() {
        return new Film(id, name, description, releaseDate, duration, 0, genres, mpa, Set.of());
    }

    private String makeDescription() {
        String description = faker.lorem().sentence(faker.random().nextInt(5, 50));
        return description.length() < 200 ? description : description.substring(0, 200);
    }
}
