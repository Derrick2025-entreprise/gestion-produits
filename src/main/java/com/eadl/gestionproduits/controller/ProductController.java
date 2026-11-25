package com.eadl.gestionproduits.controller;

import com.eadl.gestionproduits.dto.ApiError;
import com.eadl.gestionproduits.dto.PageResponse;
import com.eadl.gestionproduits.dto.ProductRequest;
import com.eadl.gestionproduits.dto.ProductResponse;
import com.eadl.gestionproduits.dto.StockAdjustmentRequest;
import com.eadl.gestionproduits.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/products")
@Validated
@Tag(name = "Produits", description = "Gestion du catalogue de produits et du stock")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Créer un produit")
    @ApiResponse(responseCode = "201", description = "Produit créé ; l'en-tête Location donne son adresse")
    @ApiResponse(responseCode = "400", description = "Données invalides",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Catégorie introuvable",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "SKU déjà utilisé",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Lister les produits (pagination, tri, filtres)")
    @ApiResponse(responseCode = "200", description = "Page de produits")
    @ApiResponse(responseCode = "400", description = "Paramètre invalide (taille, tri, prix...)",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> search(
            @Parameter(description = "Numéro de page, à partir de 0", example = "0")
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Taille de page, de 1 à 100", example = "10")
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Tri : champ,direction (ex. price,desc)", example = "price,desc")
            @SortDefault("id") Sort sort,
            @Parameter(description = "Le nom contient ce texte (sans tenir compte des majuscules)", example = "ordi")
            @RequestParam(name = "name", required = false) String name,
            @Parameter(description = "Identifiant de catégorie", example = "1")
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @Parameter(description = "Prix minimum", example = "1000")
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @Parameter(description = "Prix maximum", example = "500000")
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice) {
        PageRequest pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.search(name, categoryId, minPrice, maxPrice, pageable));
    }

    @Operation(summary = "Récupérer un produit par son identifiant")
    @ApiResponse(responseCode = "200", description = "Produit trouvé")
    @ApiResponse(responseCode = "400", description = "Identifiant non numérique",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Produit introuvable",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(
            @Parameter(description = "Identifiant du produit", example = "1") @PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "Modifier un produit (remplace toutes ses données)")
    @ApiResponse(responseCode = "200", description = "Produit modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Produit ou catégorie introuvable",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "SKU déjà utilisé par un autre produit",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @Parameter(description = "Identifiant du produit", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Operation(summary = "Ajuster le stock (entrée si delta > 0, sortie si delta < 0)")
    @ApiResponse(responseCode = "200", description = "Stock ajusté")
    @ApiResponse(responseCode = "400", description = "delta absent ou égal à 0",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Produit introuvable",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Stock insuffisant pour cette sortie",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> adjustStock(
            @Parameter(description = "Identifiant du produit", example = "1") @PathVariable("id") Long id,
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(productService.adjustStock(id, request.delta()));
    }

    @Operation(summary = "Supprimer un produit")
    @ApiResponse(responseCode = "204", description = "Produit supprimé", content = @Content)
    @ApiResponse(responseCode = "404", description = "Produit introuvable",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identifiant du produit", example = "1") @PathVariable("id") Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
