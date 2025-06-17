package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    //GET /directors - Список всех режиссёров
    public Collection<Director> getAll() {
        return directorStorage.getAll();
    }

    //GET /directors/{id}- Получение режиссёра по id
    public Director getById(int id) {
        return directorStorage.getById(id);
    }

    // POST /directors - Создание режиссёра
    public Director create(Director director) {
        log.info("Director create request received: {}", director);
        Director createdDirector = directorStorage.create(director);
        log.info("Director created successfully: {}", createdDirector);
        return createdDirector;
    }

    //PUT /directors - Изменение режиссёра
    public Director update(Director director) {
        log.info("Director update request received {}", director);
        if (director.getId() == null) {
            String reason = "id field is required";
            log.warn("Validation failed: {}", reason);
            throw new ValidationException(reason);
        }

        Director updatedDirector = directorStorage.update(director);
        log.info("Director updated successfully: {}", updatedDirector);
        return updatedDirector;
    }

    // DELETE /directors/{id} - Удаление режиссёра
    public void delete(int directorId) {
        log.info("Director delete request received {}", directorStorage.getById(directorId));
        directorStorage.delete(directorId);
        log.info("Director deleted successfully, id : {}", directorId);
    }
}
