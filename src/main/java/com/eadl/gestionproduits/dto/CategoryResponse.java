package com.eadl.gestionproduits.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Catégorie renvoyée par l'API")
public record CategoryResponse(
        @Schema(description = "Identifiant généré", example = "1")
        Long id,
        @Schema(description = "Nom de la catégorie", example = "Informatique")
        String name
) {
}
