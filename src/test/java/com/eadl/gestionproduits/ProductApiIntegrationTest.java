package com.eadl.gestionproduits;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scénarios complets sur l'application assemblée (base H2, catégories initiales).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createThenReadThenAdjustStock_fullScenario() throws Exception {
        String body = """
                {"sku":"INT-001","name":"Clavier sans fil","price":15000.00,"quantity":4,"categoryId":1}
                """;

        String location = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/products/")))
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("INT-001"))
                .andExpect(jsonPath("$.categoryName").value("Informatique"));

        mockMvc.perform(patch(location + "/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void getUnknownProduct_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/987654"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void search_whenSortFieldUnknown_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("sort", "password,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void openApiDocumentation_isPublished() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de gestion de produits"));
    }
}
