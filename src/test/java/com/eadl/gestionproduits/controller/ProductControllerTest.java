package com.eadl.gestionproduits.controller;

import com.eadl.gestionproduits.dto.PageResponse;
import com.eadl.gestionproduits.dto.ProductRequest;
import com.eadl.gestionproduits.dto.ProductResponse;
import com.eadl.gestionproduits.exception.DuplicateProductException;
import com.eadl.gestionproduits.exception.InsufficientStockException;
import com.eadl.gestionproduits.exception.ProductNotFoundException;
import com.eadl.gestionproduits.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    private static final String VALID_BODY = """
            {"sku":"LAP-001","name":"Ordinateur portable 15 pouces","description":"8 Go de RAM",
             "price":349000.00,"quantity":12,"categoryId":1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private static ProductResponse response(int quantity) {
        Instant now = Instant.parse("2026-11-23T09:30:00Z");
        return new ProductResponse(7L, "LAP-001", "Ordinateur portable 15 pouces", "8 Go de RAM",
                new BigDecimal("349000.00"), quantity, "Informatique", now, now);
    }

    @Test
    void create_whenValid_returns201WithLocation() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenReturn(response(12));

        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/products/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.categoryName").value("Informatique"));
    }

    @Test
    void create_whenInvalidData_returns400WithViolations() throws Exception {
        String body = """
                {"sku":"lap 1","name":"","price":-5,"quantity":1,"categoryId":1}
                """;

        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/v1/products"))
                .andExpect(jsonPath("$.violations[?(@.field == 'name')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field == 'price')]").exists())
                .andExpect(jsonPath("$.violations[?(@.field == 'sku')]").exists());

        verifyNoInteractions(productService);
    }

    @Test
    void create_whenJsonMalformed_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content("{\"sku\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
    }

    @Test
    void create_whenSkuAlreadyUsed_returns409() throws Exception {
        when(productService.create(any(ProductRequest.class))).thenThrow(new DuplicateProductException("LAP-001"));

        mockMvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_SKU"));
    }

    @Test
    void findById_whenFound_returns200() throws Exception {
        when(productService.findById(7L)).thenReturn(response(12));

        mockMvc.perform(get("/api/v1/products/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("LAP-001"));
    }

    @Test
    void findById_whenNotFound_returns404WithApiError() throws Exception {
        when(productService.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/products/99"))
                .andExpect(jsonPath("$.violations").isEmpty());
    }

    @Test
    void findById_whenIdNotNumeric_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH"));
    }

    @Test
    void search_withDefaults_returns200() throws Exception {
        when(productService.search(any(), any(), any(), any(), any()))
                .thenReturn(new PageResponse<>(List.of(response(12)), 0, 10, 1, 1));

        mockMvc.perform(get("/api/v1/products").param("sort", "price,desc").param("name", "ordi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("LAP-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void search_whenSizeAbove100_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void update_whenValid_returns200() throws Exception {
        when(productService.update(eq(7L), any(ProductRequest.class))).thenReturn(response(12));

        mockMvc.perform(put("/api/v1/products/7").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void adjustStock_whenValid_returns200() throws Exception {
        when(productService.adjustStock(7L, -3)).thenReturn(response(9));

        mockMvc.perform(patch("/api/v1/products/7/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(9));
    }

    @Test
    void adjustStock_whenStockInsufficient_returns409() throws Exception {
        when(productService.adjustStock(7L, -100)).thenThrow(new InsufficientStockException(12, 100));

        mockMvc.perform(patch("/api/v1/products/7/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":-100}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void adjustStock_whenDeltaIsZero_returns400() throws Exception {
        mockMvc.perform(patch("/api/v1/products/7/stock").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"delta\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void delete_whenExists_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/7"))
                .andExpect(status().isNoContent());

        verify(productService).delete(7L);
    }

    @Test
    void delete_whenNotFound_returns404() throws Exception {
        doThrow(new ProductNotFoundException(99L)).when(productService).delete(99L);

        mockMvc.perform(delete("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    void unexpectedError_returns500WithoutTechnicalDetails() throws Exception {
        when(productService.findById(1L)).thenThrow(new IllegalStateException("SQL secret detail"));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Une erreur interne est survenue"));
    }

    @Test
    void unsupportedMethod_returns405() throws Exception {
        mockMvc.perform(put("/api/v1/products"))
                .andExpect(status().isMethodNotAllowed());
    }
}
