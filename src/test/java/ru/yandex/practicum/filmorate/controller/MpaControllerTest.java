package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.testdata.FilmorateApi;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Import(FilmorateApi.class)
public class MpaControllerTest {
    @Autowired
    private FilmorateApi filmorateApi;

    @Test
    void getAllRatings() throws Exception {
        filmorateApi.getAllMpa().andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void getRatingById() throws Exception {
        filmorateApi.getMpaById(1).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").isNotEmpty());
    }


    @Test
    void genreIdMustExistOnGetRatingById() throws Exception {
        filmorateApi.getMpaById(999)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("mpa with id 999 not found"));
    }

}