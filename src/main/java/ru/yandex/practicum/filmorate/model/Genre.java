package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Genre {
    @NotNull
    private final Integer id;
    private final String name;
}
