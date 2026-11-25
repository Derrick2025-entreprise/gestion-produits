package com.eadl.gestionproduits.service;

import com.eadl.gestionproduits.dto.PageResponse;
import com.eadl.gestionproduits.dto.ProductRequest;
import com.eadl.gestionproduits.dto.ProductResponse;
import com.eadl.gestionproduits.entity.Category;
import com.eadl.gestionproduits.entity.Product;
import com.eadl.gestionproduits.exception.CategoryNotFoundException;
import com.eadl.gestionproduits.exception.DuplicateProductException;
import com.eadl.gestionproduits.exception.InsufficientStockException;
import com.eadl.gestionproduits.exception.InvalidParameterException;
import com.eadl.gestionproduits.exception.ProductNotFoundException;
import com.eadl.gestionproduits.repository.CategoryRepository;
import com.eadl.gestionproduits.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService service;

    private static Category category() {
        Category category = new Category("Informatique");
        category.setId(1L);
        return category;
    }

    private static Product product(int quantity) {
        Product product = new Product();
        product.setId(7L);
        product.setSku("LAP-001");
        product.setName("Ordinateur portable");
        product.setPrice(new BigDecimal("349000.00"));
        product.setQuantity(quantity);
        product.setCategory(category());
        return product;
    }

    private static ProductRequest request(String sku) {
        return new ProductRequest(sku, "Ordinateur portable", null, new BigDecimal("349000.00"), 12, 1L);
    }

    @Test
    void create_whenValid_savesAndReturnsProduct() {
        when(productRepository.existsBySku("LAP-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(productRepository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));

        ProductResponse response = service.create(request("LAP-001"));

        assertThat(response.sku()).isEqualTo("LAP-001");
        assertThat(response.categoryName()).isEqualTo("Informatique");
    }

    @Test
    void create_whenSkuAlreadyUsed_throwsDuplicateProductException() {
        when(productRepository.existsBySku("LAP-001")).thenReturn(true);

        ProductRequest request = request("LAP-001");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(DuplicateProductException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_whenCategoryUnknown_throwsCategoryNotFoundException() {
        when(productRepository.existsBySku("LAP-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        ProductRequest request = request("LAP-001");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void findById_whenProductExists_returnsProduct() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product(12)));

        ProductResponse response = service.findById(7L);

        assertThat(response.id()).isEqualTo(7L);
    }

    @Test
    void findById_whenProductDoesNotExist_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void search_returnsPageOfProducts() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(productRepository.search("ordi", null, null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(product(12)), pageable, 1));

        PageResponse<ProductResponse> page = service.search("ordi", null, null, null, pageable);

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void search_whenSortFieldNotAllowed_throwsInvalidParameterException() {
        PageRequest pageable = PageRequest.of(0, 10, Sort.by("password"));

        assertThatThrownBy(() -> service.search(null, null, null, null, pageable))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessageContaining("password");
    }

    @Test
    void update_whenValid_replacesData() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product(12)));
        when(productRepository.existsBySku("LAP-002")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category()));
        when(productRepository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));

        ProductResponse response = service.update(7L, request("LAP-002"));

        assertThat(response.sku()).isEqualTo("LAP-002");
    }

    @Test
    void update_whenNewSkuBelongsToAnotherProduct_throwsDuplicateProductException() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product(12)));
        when(productRepository.existsBySku("LAP-002")).thenReturn(true);

        ProductRequest request = request("LAP-002");

        assertThatThrownBy(() -> service.update(7L, request))
                .isInstanceOf(DuplicateProductException.class);
    }

    @Test
    void delete_whenProductExists_deletesIt() {
        Product product = product(12);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));

        service.delete(7L);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_whenProductDoesNotExist_throwsProductNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void adjustStock_whenEntry_increasesQuantity() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product(12)));
        when(productRepository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));

        ProductResponse response = service.adjustStock(7L, 5);

        assertThat(response.quantity()).isEqualTo(17);
    }

    @Test
    void adjustStock_whenValidExit_decreasesQuantity() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product(12)));
        when(productRepository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));

        ProductResponse response = service.adjustStock(7L, -12);

        assertThat(response.quantity()).isZero();
    }

    @Test
    void adjustStock_whenExitExceedsStock_throwsInsufficientStockException() {
        when(productRepository.findById(7L)).thenReturn(Optional.of(product(12)));

        assertThatThrownBy(() -> service.adjustStock(7L, -13))
                .isInstanceOf(InsufficientStockException.class);
        verify(productRepository, never()).save(any());
    }
}
