package com.eadl.gestionproduits.exception;

public final class ErrorCodes {

    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String CATEGORY_NOT_FOUND = "CATEGORY_NOT_FOUND";
    public static final String DUPLICATE_SKU = "DUPLICATE_SKU";
    public static final String DUPLICATE_CATEGORY = "DUPLICATE_CATEGORY";
    public static final String INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    public static final String MALFORMED_JSON = "MALFORMED_JSON";
    public static final String TYPE_MISMATCH = "TYPE_MISMATCH";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    private ErrorCodes() {
    }
}
