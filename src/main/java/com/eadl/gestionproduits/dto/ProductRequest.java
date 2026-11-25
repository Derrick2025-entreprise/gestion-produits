package com.eadl.gestionproduits.dto;

import com.eadl.gestionproduits.entity.ProductLimits;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Données d'un produit à créer ou à modifier")
public record ProductRequest(

        @Schema(description = "Référence unique du produit (RG2)", example = "LAP-001")
        @NotBlank(message = "La référence est obligatoire")
        @Pattern(regexp = ProductLimits.SKU_PATTERN,
                message = "Format invalide : 3 à 20 caractères parmi A-Z, 0-9 et -")
        String sku,

        @Schema(description = "Nom du produit (RG1)", example = "Ordinateur portable 15 pouces")
        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = ProductLimits.NAME_MIN_LENGTH, max = ProductLimits.NAME_MAX_LENGTH,
                message = "Le nom doit contenir entre 3 et 100 caractères")
        String name,

        @Schema(description = "Description facultative (RG8)", example = "8 Go de RAM, SSD 256 Go")
        @Size(max = ProductLimits.DESCRIPTION_MAX_LENGTH,
                message = "La description ne doit pas dépasser 500 caractères")
        String description,

        @Schema(description = "Prix strictement positif, deux décimales au plus (RG3)", example = "349000.00")
        @NotNull(message = "Le prix est obligatoire")
        @Positive(message = "Le prix doit être supérieur à 0")
        @Digits(integer = ProductLimits.PRICE_INTEGER_DIGITS, fraction = ProductLimits.PRICE_FRACTION_DIGITS,
                message = "Le prix doit avoir au plus 2 décimales")
        BigDecimal price,

        @Schema(description = "Quantité en stock, supérieure ou égale à 0 (RG4)", example = "12")
        @NotNull(message = "La quantité est obligatoire")
        @Min(value = 0, message = "La quantité doit être supérieure ou égale à 0")
        Integer quantity,

        @Schema(description = "Identifiant d'une catégorie existante (RG5)", example = "1")
        @NotNull(message = "La catégorie est obligatoire")
        Long categoryId
) {
}
