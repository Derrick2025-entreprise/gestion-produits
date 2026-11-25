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
import com.eadl.gestionproduits.mapper.ProductMapper;
import com.eadl.gestionproduits.repository.CategoryRepository;
import com.eadl.gestionproduits.repository.ProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Règles de gestion des produits (F1 à F6, RG1 à RG8).
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "sku", "name", "price", "quantity", "createdAt", "updatedAt");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * F1 : crée un produit.
     *
     * @throws DuplicateProductException  si le SKU est déjà utilisé (RG2)
     * @throws CategoryNotFoundException si la catégorie n'existe pas (RG5)
     */
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateProductException(request.sku());
        }
        Category category = getCategoryOrThrow(request.categoryId());
        Product saved = productRepository.save(ProductMapper.toEntity(request, category));
        return ProductMapper.toResponse(saved);
    }

    /**
     * F2 : renvoie un produit.
     *
     * @throws ProductNotFoundException si le produit n'existe pas (RG7)
     */
    public ProductResponse findById(Long id) {
        return ProductMapper.toResponse(getProductOrThrow(id));
    }

    /**
     * F3 : liste paginée et triée ; chaque filtre (nom, catégorie, prix min et max) est facultatif.
     *
     * @throws InvalidParameterException si le tri porte sur un champ non autorisé
     */
    public PageResponse<ProductResponse> search(String name, Long categoryId,
                                                BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new InvalidParameterException("Tri impossible sur le champ " + order.getProperty());
            }
        });
        return PageResponse.from(
                productRepository.search(name, categoryId, minPrice, maxPrice, pageable),
                ProductMapper::toResponse);
    }

    /**
     * F4 : remplace toutes les données modifiables d'un produit.
     *
     * @throws ProductNotFoundException  si le produit n'existe pas (RG7)
     * @throws DuplicateProductException  si le nouveau SKU appartient à un autre produit (RG2)
     * @throws CategoryNotFoundException si la catégorie n'existe pas (RG5)
     */
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProductOrThrow(id);
        boolean skuChanged = !product.getSku().equals(request.sku());
        if (skuChanged && productRepository.existsBySku(request.sku())) {
            throw new DuplicateProductException(request.sku());
        }
        Category category = getCategoryOrThrow(request.categoryId());
        ProductMapper.updateEntity(product, request, category);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    /**
     * F5 : ajoute (delta positif) ou retire (delta négatif) une quantité au stock.
     *
     * @throws ProductNotFoundException    si le produit n'existe pas (RG7)
     * @throws InsufficientStockException si la sortie dépasse le stock disponible (RG6)
     */
    @Transactional
    public ProductResponse adjustStock(Long id, int delta) {
        Product product = getProductOrThrow(id);
        int newQuantity = product.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new InsufficientStockException(product.getQuantity(), -delta);
        }
        product.setQuantity(newQuantity);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    /**
     * F6 : supprime un produit.
     *
     * @throws ProductNotFoundException si le produit n'existe pas (RG7)
     */
    @Transactional
    public void delete(Long id) {
        productRepository.delete(getProductOrThrow(id));
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
