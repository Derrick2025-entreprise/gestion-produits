package com.eadl.gestionproduits.repository;

import com.eadl.gestionproduits.entity.Category;
import com.eadl.gestionproduits.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category informatique;
    private Category telephonie;

    @BeforeEach
    void setUp() {
        informatique = categoryRepository.save(new Category("Ordinateurs (test)"));
        telephonie = categoryRepository.save(new Category("Mobiles (test)"));
        productRepository.save(product("LAP-001", "Ordinateur portable 15 pouces", "349000.00", informatique));
        productRepository.save(product("LAP-002", "Ordinateur de bureau", "520000.00", informatique));
        productRepository.save(product("PHO-001", "Téléphone Android", "120000.00", telephonie));
    }

    private static Product product(String sku, String name, String price, Category category) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setQuantity(5);
        product.setCategory(category);
        return product;
    }

    @Test
    void existsBySku_whenSkuExists_returnsTrue() {
        assertThat(productRepository.existsBySku("LAP-001")).isTrue();
    }

    @Test
    void existsBySku_whenSkuUnknown_returnsFalse() {
        assertThat(productRepository.existsBySku("XXX-999")).isFalse();
    }

    @Test
    void findBySku_returnsProductWithTimestamps() {
        Product found = productRepository.findBySku("PHO-001").orElseThrow();

        assertThat(found.getName()).isEqualTo("Téléphone Android");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void search_byPartialNameIgnoringCase_returnsMatches() {
        Page<Product> page = productRepository.search("ORDI", null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Product::getSku).containsExactlyInAnyOrder("LAP-001", "LAP-002");
    }

    @Test
    void search_byCategoryAndPriceRange_returnsMatches() {
        Page<Product> page = productRepository.search(null, informatique.getId(),
                new BigDecimal("300000"), new BigDecimal("400000"), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Product::getSku).containsExactly("LAP-001");
    }

    @Test
    void search_withoutFilters_paginatesAndSorts() {
        Page<Product> page = productRepository.search(null, null, null, null,
                PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "price")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(Product::getSku).containsExactly("LAP-002", "LAP-001");
    }

    @Test
    void save_whenSkuDuplicated_violatesUniqueConstraint() {
        Product duplicate = product("LAP-001", "Autre ordinateur", "1000.00", informatique);

        assertThatThrownBy(() -> productRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
