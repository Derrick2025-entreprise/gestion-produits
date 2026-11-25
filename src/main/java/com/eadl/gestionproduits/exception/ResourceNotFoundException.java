package com.eadl.gestionproduits.exception;


public abstract class ResourceNotFoundException extends BusinessException {

    protected ResourceNotFoundException(String code, String message) {
        super(code, message);
    }
}
