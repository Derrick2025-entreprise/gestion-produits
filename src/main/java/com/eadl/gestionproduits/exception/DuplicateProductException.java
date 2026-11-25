package com.eadl.gestionproduits.exception;


public class DuplicateProductException extends ConflictException {

    public DuplicateProductException(String sku) {
        super(ErrorCodes.DUPLICATE_SKU, "Un produit existe déjà avec le SKU " + sku);
    }
}
