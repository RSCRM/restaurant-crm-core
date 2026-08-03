package com.restaurant.crm.modules.erp.menu.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateProductRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.ProductSearchRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateProductRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductManagementService {
    ProductResponse create(CreateProductRequest request, MultipartFile image);
    ProductResponse update(String id, UpdateProductRequest request, MultipartFile image);
    void delete(String id);
    List<ProductResponse> listByBranch(String branchId);
    ProductResponse get(String id);
    PagingResponse<ProductResponse> searchProducts(ProductSearchRequest searchRequest, PagingRequest pagingRequest);
}
