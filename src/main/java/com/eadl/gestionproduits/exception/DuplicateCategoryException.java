package com.eadl.gestionproduits.exception;


public class DuplicateCategoryException extends BusinessException {

    public DuplicateCategoryException(String name) {
        super(ErrorCodes.DUPLICATE_CATEGORY, "Une catégorie existe déjà avec le nom " + name);
    }
}
