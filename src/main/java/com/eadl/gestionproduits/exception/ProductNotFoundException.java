package com.eadl.gestionproduits.exception;


public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long id) {
        super(ErrorCodes.PRODUCT_NOT_FOUND, "Produit introuvable : id=" + id);
    }
}
