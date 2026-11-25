package com.eadl.gestionproduits.entity;


public final class ProductLimits {

    public static final int NAME_MIN_LENGTH = 3;
    public static final int NAME_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
    public static final int SKU_MAX_LENGTH = 20;
    public static final String SKU_PATTERN = "^[A-Z0-9-]{3,20}$";
    public static final int PRICE_INTEGER_DIGITS = 10;
    public static final int PRICE_FRACTION_DIGITS = 2;
    public static final int CATEGORY_NAME_MAX_LENGTH = 100;

    private ProductLimits() {
    }
}
