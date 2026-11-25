package com.eadl.gestionproduits.exception;


public class CategoryNotFoundException extends ResourceNotFoundException {

    public CategoryNotFoundException(Long id) {
        super(ErrorCodes.CATEGORY_NOT_FOUND, "Catégorie introuvable : id=" + id);
    }
}
