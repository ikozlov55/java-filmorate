package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private Integer id;
    @NotBlank
    @Email
    private String email;
    @NotNull
    @Pattern(regexp = "\\S+")
    private String login;
    private String name;
    @NotNull
    @Past
    private LocalDate birthday;
}

