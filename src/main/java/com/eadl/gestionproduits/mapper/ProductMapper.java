package com.eadl.gestionproduits.mapper;

import com.eadl.gestionproduits.dto.ProductRequest;
import com.eadl.gestionproduits.dto.ProductResponse;
import com.eadl.gestionproduits.entity.Category;
import com.eadl.gestionproduits.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toEntity(ProductRequest request, Category category) {
        Product product = new Product();
        copy(request, category, product);
        return product;
    }

    public static void updateEntity(Product product, ProductRequest request, Category category) {
        copy(request, category, product);
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    private static void copy(ProductRequest request, Category category, Product product) {
        product.setSku(request.sku());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        product.setCategory(category);
    }
}
