package com.restaurant.crm.modules.erp.menu.constants;

public final class MenuStorageConstants {
    private MenuStorageConstants() {}

    public static final String PRODUCT_PATH = "products/";
    public static final String COMBO_PATH = "combos/";
    public static final String IMAGE_FILE_NAME = "main";


    public static String productImagePublicId(String productId) {
        return PRODUCT_PATH + productId + "/" + IMAGE_FILE_NAME;
    }

    public static String comboImagePublicId(String comboId) {
        return COMBO_PATH + comboId + "/" + IMAGE_FILE_NAME;
    }
}
