package com.restaurant.crm.modules.erp.menu.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.menu.dto.request.ComboItemRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.ComboSearchRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateComboRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateComboRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboItemResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboResponse;

import java.util.List;

public interface ComboManagementService {
    ComboResponse create(CreateComboRequest request);
    ComboResponse update(String id, UpdateComboRequest request);
    void delete(String id);
    List<ComboResponse> listByBranch(String branchId);
    ComboResponse get(String id);
    PagingResponse<ComboResponse> searchCombos(ComboSearchRequest searchRequest, PagingRequest pagingRequest);
    ComboItemResponse addItem(String comboId, ComboItemRequest request);
    ComboItemResponse updateItem(String itemId, ComboItemRequest request);
    void deleteItem(String itemId);
}
