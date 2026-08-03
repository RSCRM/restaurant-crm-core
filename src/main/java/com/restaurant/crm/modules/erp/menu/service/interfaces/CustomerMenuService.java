package com.restaurant.crm.modules.erp.menu.service.interfaces;

import com.restaurant.crm.modules.erp.menu.dto.response.CustomerMenuResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuProductResponse;


public interface CustomerMenuService {


    CustomerMenuResponse getMenu();


    MenuProductResponse getProduct(String productId);
}
