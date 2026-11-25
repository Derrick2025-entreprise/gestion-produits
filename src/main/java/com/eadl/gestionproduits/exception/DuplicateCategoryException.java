package com.eadl.gestionproduits.exception;


public class DuplicateCategoryException extends ConflictException {

    public DuplicateCategoryException(String name) {
        super(ErrorCodes.DUPLICATE_CATEGORY, "Une catégorie existe déjà avec le nom " + name);
    }
}
