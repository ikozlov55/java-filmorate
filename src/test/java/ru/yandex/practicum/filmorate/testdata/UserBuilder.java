package ru.yandex.practicum.filmorate.testdata;

import net.datafaker.Faker;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Locale;

public class UserBuilder {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    Faker faker = new Faker(Locale.of("RU"));

    public UserBuilder() {
        email = faker.internet().emailAddress();
        login = faker.word().noun();
        name = faker.name().name();
        birthday = faker.timeAndDate().birthday();
    }

    public UserBuilder id(int id) {
        this.id = id;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder login(String login) {
        this.login = login;
        return this;
    }

    public UserBuilder name(String name) {
        this.name = name;
        return this;
    }

    public UserBuilder birthday(LocalDate birthday) {
        this.birthday = birthday;
        return this;
    }

    public User build() {
        return new User(id, email, login, name, birthday);
    }
}
