package com.eadl.gestionproduits.dto;

import com.eadl.gestionproduits.entity.ProductLimits;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Catégorie à créer")
public record CategoryRequest(
        @Schema(description = "Nom unique de la catégorie", example = "Informatique")
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = ProductLimits.CATEGORY_NAME_MAX_LENGTH,
                message = "Le nom ne doit pas dépasser 100 caractères")
        String name
) {
}
