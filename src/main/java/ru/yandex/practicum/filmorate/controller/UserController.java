package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private static int nextEntityId = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("User create request received: {}", user);
        user.setId(nextEntityId);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        nextEntityId++;
        log.info("User created successfully: {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("User update request received {}", user);
        if (user.getId() == null) {
            String reason = "id field is required";
            log.warn("Validation failed: {}", reason);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
        }
        if (!users.containsKey(user.getId())) {
            String reason = String.format("user with id %d not found", user.getId());
            log.warn("Validation failed: {}", reason);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, reason);
        }
        User oldUser = users.get(user.getId());
        oldUser.setEmail(user.getEmail());
        oldUser.setLogin(user.getLogin());
        oldUser.setName(user.getName() != null ? user.getName() : user.getLogin());
        oldUser.setBirthday(user.getBirthday());
        log.info("User updated successfully: {}", user);
        return oldUser;
    }
}
