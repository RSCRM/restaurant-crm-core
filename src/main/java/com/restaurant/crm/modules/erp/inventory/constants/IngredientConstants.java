package com.restaurant.crm.modules.erp.inventory.constants;

public final class IngredientConstants {

    private IngredientConstants() {}

    public static final String TABLE_INGREDIENT = "ingredients";

    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_INGREDIENT_CATEGORY_ID = "ingredient_category_id";
    public static final String COL_INGREDIENT_NAME = "ingredient_name";
    public static final String COL_UNIT = "unit";
    public static final String COL_DESCRIPTION = "description";

    public static final String INGREDIENT_NAME_DEFINITION = "VARCHAR(150)";
    public static final String UNIT_DEFINITION = "VARCHAR(20)";
    public static final String DESCRIPTION_DEFINITION = "VARCHAR(255)";

    public static final int MAX_CHARS_INGREDIENT_NAME = 150;
    public static final int MAX_CHARS_UNIT = 20;
    public static final int MAX_CHARS_DESCRIPTION = 255;
}
