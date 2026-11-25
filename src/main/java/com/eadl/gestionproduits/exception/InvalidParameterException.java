package com.eadl.gestionproduits.exception;

public class InvalidParameterException extends BusinessException {

    public InvalidParameterException(String message) {
        super(ErrorCodes.INVALID_PARAMETER, message);
    }
}
