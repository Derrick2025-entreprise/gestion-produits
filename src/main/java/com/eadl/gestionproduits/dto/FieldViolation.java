package com.eadl.gestionproduits.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Erreur de validation sur un champ")
public record FieldViolation(
        @Schema(description = "Champ ou paramètre en erreur", example = "price")
        String field,
        @Schema(description = "Explication de l'erreur", example = "Le prix doit être supérieur à 0")
        String message
) {
}
