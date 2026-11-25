package com.eadl.gestionproduits.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Ajustement du stock : positif pour une entrée, négatif pour une sortie")
public record StockAdjustmentRequest(
        @Schema(description = "Quantité à ajouter (positive) ou à retirer (négative), jamais 0", example = "-3")
        @NotNull(message = "delta est obligatoire")
        Integer delta
) {

    @Schema(hidden = true)
    @AssertTrue(message = "delta ne doit pas être égal à 0")
    public boolean isDeltaNonZero() {
        return delta == null || delta != 0;
    }
}
