package com.restaurant.crm.modules.erp.menu.service.interfaces;

import com.restaurant.crm.modules.erp.menu.dto.response.CustomerMenuResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuProductResponse;

/**
 * Read-only digital menu for a seated customer (uc-c-04).
 * The branch is always taken from the session token (NFR-07) — never from the client.
 */
public interface CustomerMenuService {

    /**
     * Returns the full menu (categories → products, combos, modifier groups) of the caller's
     * branch in one shot (uc-c-04). Throws {@code MENU_BRANCH_CONTEXT_MISSING} if the session
     * has no branch, {@code MENU_EMPTY} if the branch has neither products nor combos.
     */
    CustomerMenuResponse getMenu();

    /**
     * Returns one product of the caller's branch (uc-c-04).
     * Throws {@code MENU_PRODUCT_NOT_FOUND} if it does not exist in this branch.
     */
    MenuProductResponse getProduct(String productId);
}
