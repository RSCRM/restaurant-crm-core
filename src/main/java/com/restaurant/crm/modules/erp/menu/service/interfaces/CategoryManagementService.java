package com.restaurant.crm.modules.erp.menu.service.interfaces;

import com.restaurant.crm.modules.erp.menu.dto.request.CreateCategoryRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateCategoryRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryManagementService {
    CategoryResponse create(CreateCategoryRequest request);
    CategoryResponse update(String id, UpdateCategoryRequest request);
    void delete(String id);
    List<CategoryResponse> listByBranch(String branchId);
    CategoryResponse get(String id);
}
