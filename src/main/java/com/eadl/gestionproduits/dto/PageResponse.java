package com.eadl.gestionproduits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Schema(description = "Une page de résultats")
public record PageResponse<T>(
        @Schema(description = "Éléments de la page")
        List<T> content,
        @Schema(description = "Numéro de la page (à partir de 0)", example = "0")
        int page,
        @Schema(description = "Taille de la page", example = "10")
        int size,
        @Schema(description = "Nombre total d'éléments", example = "42")
        long totalElements,
        @Schema(description = "Nombre total de pages", example = "5")
        int totalPages
) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
