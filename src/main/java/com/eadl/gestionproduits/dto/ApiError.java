package com.eadl.gestionproduits.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Format unique de toutes les réponses d'erreur")
public record ApiError(
        @Schema(description = "Horodatage de l'erreur (UTC)", example = "2026-11-23T09:35:12Z")
        Instant timestamp,
        @Schema(description = "Statut HTTP", example = "404")
        int status,
        @Schema(description = "Code d'erreur stable, exploitable par un programme", example = "PRODUCT_NOT_FOUND")
        String code,
        @Schema(description = "Message lisible", example = "Produit introuvable : id=99")
        String message,
        @Schema(description = "Chemin de la requête", example = "/api/v1/products/99")
        String path,
        @Schema(description = "Erreurs par champ (vide si aucune)")
        List<FieldViolation> violations
) {
}
