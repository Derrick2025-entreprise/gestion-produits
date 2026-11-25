package com.eadl.gestionproduits.service;

import com.eadl.gestionproduits.dto.CategoryRequest;
import com.eadl.gestionproduits.dto.CategoryResponse;
import com.eadl.gestionproduits.entity.Category;
import com.eadl.gestionproduits.exception.DuplicateCategoryException;
import com.eadl.gestionproduits.mapper.CategoryMapper;
import com.eadl.gestionproduits.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des catégories (F7).
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Crée une catégorie.
     *
     * @throws DuplicateCategoryException si une catégorie porte déjà ce nom
     */
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateCategoryException(name);
        }
        return CategoryMapper.toResponse(categoryRepository.save(new Category(name)));
    }

    /**
     * Liste toutes les catégories, triées par nom.
     */
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }
}
