package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorStorage {
    Collection<Director> getAll(); //GET /directors - Список всех режиссёров

    Director getById(int id); //GET /directors/{id}- Получение режиссёра по id

    Director create(Director director); // POST /directors - Создание режиссёра

    Director update(Director director); //PUT /directors - Изменение режиссёра

    void delete(int directorId); // DELETE /directors/{id} - Удаление режиссёра

    void checkDirectorExists(int id);
}
