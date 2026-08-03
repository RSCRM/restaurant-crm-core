package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.GroupCartAddItemRequest;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartUpdateItemRequest;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartSubmitResponse;


public interface GroupCartService {


    GroupCartResponse getCart();


    GroupCartResponse addItem(GroupCartAddItemRequest request);


    GroupCartResponse updateItem(String cartItemId, GroupCartUpdateItemRequest request);


    GroupCartResponse deleteItem(String cartItemId);


    void lockItem(String cartItemId);


    void unlockItem(String cartItemId);

    GroupCartSubmitResponse submit();
}

