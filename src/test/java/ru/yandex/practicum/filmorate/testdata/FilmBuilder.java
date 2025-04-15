package ru.yandex.practicum.filmorate.testdata;

import net.datafaker.Faker;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Locale;

public class FilmBuilder {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    Faker faker = new Faker(Locale.of("RU"));

    public FilmBuilder() {
        name = faker.movie().name();
        description = makeDescription();
        releaseDate = faker.timeAndDate().birthday(0, 40);
        duration = faker.random().nextInt(60, 120);
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

    public Film build() {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        return film;
    }

    private String makeDescription() {
        String description = faker.lorem().sentence(faker.random().nextInt(5, 50));
        return description.length() < 200 ? description : description.substring(0, 200);
    }
}
