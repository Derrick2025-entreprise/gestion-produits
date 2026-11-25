package com.eadl.gestionproduits.service;

import com.eadl.gestionproduits.dto.CategoryRequest;
import com.eadl.gestionproduits.dto.CategoryResponse;
import com.eadl.gestionproduits.entity.Category;
import com.eadl.gestionproduits.exception.DuplicateCategoryException;
import com.eadl.gestionproduits.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_whenNameIsNew_savesTrimmedName() {
        when(categoryRepository.existsByNameIgnoreCase("Jardin")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(4L);
            return saved;
        });

        CategoryResponse response = categoryService.create(new CategoryRequest("  Jardin "));

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.name()).isEqualTo("Jardin");
    }

    @Test
    void create_whenNameAlreadyUsed_throwsDuplicateCategoryException() {
        when(categoryRepository.existsByNameIgnoreCase("Informatique")).thenReturn(true);

        CategoryRequest request = new CategoryRequest("Informatique");

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateCategoryException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void findAll_returnsCategoriesSortedByName() {
        Category informatique = new Category("Informatique");
        informatique.setId(1L);
        when(categoryRepository.findAll(Sort.by("name"))).thenReturn(List.of(informatique));

        List<CategoryResponse> categories = categoryService.findAll();

        assertThat(categories).extracting(CategoryResponse::name).containsExactly("Informatique");
    }
}
