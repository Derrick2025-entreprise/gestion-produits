package com.eadl.gestionproduits.controller;

import com.eadl.gestionproduits.dto.CategoryRequest;
import com.eadl.gestionproduits.dto.CategoryResponse;
import com.eadl.gestionproduits.exception.DuplicateCategoryException;
import com.eadl.gestionproduits.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void create_whenValid_returns201() throws Exception {
        when(categoryService.create(any(CategoryRequest.class))).thenReturn(new CategoryResponse(4L, "Jardin"));

        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Jardin\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Jardin"));
    }

    @Test
    void create_whenNameBlank_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("name"));
    }

    @Test
    void create_whenNameAlreadyUsed_returns409() throws Exception {
        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new DuplicateCategoryException("Informatique"));

        mockMvc.perform(post("/api/v1/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Informatique\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CATEGORY"));
    }

    @Test
    void findAll_returns200() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(new CategoryResponse(1L, "Informatique")));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Informatique"));
    }
}
