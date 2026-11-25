package com.eadl.gestionproduits.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(int available, int requested) {
        super(ErrorCodes.INSUFFICIENT_STOCK,
                "Stock insuffisant : disponible=" + available + ", demandé=" + requested);
    }
}
