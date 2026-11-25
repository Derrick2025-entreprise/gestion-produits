package com.eadl.gestionproduits.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Produit renvoyé par l'API")
public record ProductResponse(
        @Schema(description = "Identifiant généré", example = "7")
        Long id,
        @Schema(description = "Référence unique", example = "LAP-001")
        String sku,
        @Schema(description = "Nom du produit", example = "Ordinateur portable 15 pouces")
        String name,
        @Schema(description = "Description", example = "8 Go de RAM, SSD 256 Go")
        String description,
        @Schema(description = "Prix", example = "349000.00")
        BigDecimal price,
        @Schema(description = "Quantité en stock", example = "12")
        int quantity,
        @Schema(description = "Nom de la catégorie", example = "Informatique")
        String categoryName,
        @Schema(description = "Date de création (UTC)", example = "2026-11-23T09:30:00Z")
        Instant createdAt,
        @Schema(description = "Date de dernière modification (UTC)", example = "2026-11-23T09:30:00Z")
        Instant updatedAt
) {
}
