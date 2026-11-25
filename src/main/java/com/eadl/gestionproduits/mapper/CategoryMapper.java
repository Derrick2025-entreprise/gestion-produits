package com.eadl.gestionproduits.mapper;

import com.eadl.gestionproduits.dto.CategoryResponse;
import com.eadl.gestionproduits.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
