package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    /*
    Класс SimpleJdbcInsert из Spring Framework упрощает процесс вставки данных в базу данных.
    Создаёт объект SimpleJdbcInsert, связанный с определённой таблицей в базе данных.
Автоматически генерирует SQL-запрос для вставки данных на основе метаданных таблицы.
Вам не нужно вручную писать SQL-запросы для вставки, что уменьшает вероятность ошибок и делает код более читаемым.
Позволяет передавать параметры для вставки в виде карты (Map), где ключами являются имена столбцов,
а значениями — соответствующие данные.
Выполняет запрос на вставку и возвращает сгенерированный первичный ключ для вставленной записи
(если таблица поддерживает автоинкремент).
     */
    private final SimpleJdbcInsert directorsJdbcInsert;
    private static final String SELECT_DIRECTORS_QUERY = """
            SELECT id,
                   name
              FROM directors
                   %s
            """;

    //GET /directors - Список всех режиссёров
    @Override
    public Collection<Director> getAll() {
        return jdbcTemplate.query(String.format(SELECT_DIRECTORS_QUERY, ""), DirectorMapper.getInstance());
    }

    //GET /directors/{id}- Получение режиссёра по id
    @Override
    public Director getById(int id) {
        checkDirectorExists(id);
        String query = String.format(SELECT_DIRECTORS_QUERY, "WHERE id = ?");
        return jdbcTemplate.queryForObject(String.format(query), DirectorMapper.getInstance(), id);
    }

    // POST /directors - Создание режиссёра
    @Override
    public Director create(Director director) {
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("name", director.getName());
//выполнение SQL-запроса для вставки данных о режиссере в базу данных и получение сгенерированного идентификатора фильма
        int directorId = directorsJdbcInsert.executeAndReturnKey(argsMap).intValue();
        director.setId(directorId);
        return director;
    }

    //PUT /directors - Изменение режиссёра
    @Override
    public Director update(Director director) {
        checkDirectorExists(director.getId());
        jdbcTemplate.update("""
                UPDATE directors
                   SET name = ?
                 WHERE id = ?
                """, director.getName(), director.getId());
        return getById(director.getId());

    }

    // DELETE /directors/{id} - Удаление режиссёра
    @Override
    public void delete(int directorId) {
        checkDirectorExists(directorId);
        jdbcTemplate.update("DELETE FROM films_directors WHERE director_id = ?", directorId);
        jdbcTemplate.update("DELETE FROM directors WHERE id = ?", directorId);
    }

    @Override
    public void checkDirectorExists(int id) {
        /*
        Запрос используется для проверки существования записи в таблице directors с определённым id.
Конструкция EXISTS (SELECT 1 ...) проверяет, существует ли хотя бы одна строка в результате вложенного запроса.
Если запись с указанным id найдена, запрос вернёт true, иначе — false.
         */
        String query = "SELECT EXISTS (SELECT 1 FROM directors WHERE id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(query, Boolean.class, id);
        if (exists == null || !exists) {
            String reason = String.format("Director with id %d not found", id);
            log.warn("Validation failed: {}", reason);
            throw new NotFoundException(reason);
        }
    }
}
