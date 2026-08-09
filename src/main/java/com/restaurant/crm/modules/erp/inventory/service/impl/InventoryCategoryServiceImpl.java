package com.restaurant.crm.modules.erp.inventory.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryCategoryStatusRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryCategoryResponse;
import com.restaurant.crm.modules.erp.inventory.entity.InventoryCategory;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryCategoryStatus;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import com.restaurant.crm.modules.erp.inventory.mapper.InventoryCategoryMapper;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryCategoryRepository;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryRepository;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryCategoryService;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryCategoryServiceImpl implements InventoryCategoryService {

    InventoryCategoryRepository inventoryCategoryRepository;
    OrganizationBranchRepository organizationBranchRepository;
    InventoryCategoryMapper inventoryCategoryMapper;
    InventoryRepository inventoryRepository;
    @Override
    @Transactional
    public InventoryCategoryResponse createInventoryCategory(
        CreateInventoryCategoryRequest request
    ) {

        String branchId = AuthUtils.getBranchId();

        OrganizationBranch branch =
            organizationBranchRepository.findById(branchId)
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND
                    )
                );

        if (inventoryCategoryRepository.existsByBranchIdAndCategoryName(
            branchId,
            request.getCategoryName()
        )) {
            throw new AppException(
                ErrorCode.INVENTORY_CATEGORY_EXISTS
            );
        }

        InventoryCategory inventoryCategory =
            inventoryCategoryMapper.toInventoryCategory(request);

        inventoryCategory.setBranch(branch);

        inventoryCategory =
            inventoryCategoryRepository.save(inventoryCategory);

        return inventoryCategoryMapper.toInventoryCategoryResponse(
            inventoryCategory
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryCategoryResponse getInventoryCategoryById(
        String id
    ) {

        String branchId = AuthUtils.getBranchId();

        InventoryCategory inventoryCategory =
            inventoryCategoryRepository.findByIdAndBranchId(
                    id,
                    branchId
                )
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INVENTORY_CATEGORY_NOT_FOUND
                    )
                );

        return inventoryCategoryMapper.toInventoryCategoryResponse(
            inventoryCategory
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryCategoryResponse> getInventoryCategories(
        int page,
        int size
    ) {

        String branchId = AuthUtils.getBranchId();

        Pageable pageable = PageRequest.of(
            page - GlobalVariableConstant.PAGE_SIZE_INDEX,
            size,
            Sort.by(Sort.Order.asc("categoryName"))
        );

        Page<InventoryCategory> categoryPage =
            inventoryCategoryRepository.findByBranchId(
                branchId,
                pageable
            );

        return PagingResponse.<InventoryCategoryResponse>builder()
            .currentPage(page)
            .pageSize(categoryPage.getSize())
            .totalPages(categoryPage.getTotalPages())
            .totalElement(categoryPage.getTotalElements())
            .data(
                categoryPage.getContent()
                    .stream()
                    .map(inventoryCategoryMapper::toInventoryCategoryResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryCategoryResponse> searchInventoryCategories(
        String categoryName,
        int page,
        int size
    ) {

        String branchId = AuthUtils.getBranchId();

        Pageable pageable = PageRequest.of(
            page - GlobalVariableConstant.PAGE_SIZE_INDEX,
            size,
            Sort.by(Sort.Order.asc("categoryName"))
        );

        Page<InventoryCategory> categoryPage;

        if (categoryName == null || categoryName.isBlank()) {
            categoryPage = inventoryCategoryRepository.findByBranchId(
                branchId,
                pageable
            );
        } else {
            categoryPage =
                inventoryCategoryRepository
                    .findByBranchIdAndCategoryNameContainingIgnoreCase(
                        branchId,
                        categoryName,
                        pageable
                    );
        }

        return PagingResponse.<InventoryCategoryResponse>builder()
            .currentPage(page)
            .pageSize(categoryPage.getSize())
            .totalPages(categoryPage.getTotalPages())
            .totalElement(categoryPage.getTotalElements())
            .data(
                categoryPage.getContent()
                    .stream()
                    .map(inventoryCategoryMapper::toInventoryCategoryResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional
    public InventoryCategoryResponse updateInventoryCategory(
        String id,
        UpdateInventoryCategoryRequest request
    ) {

        String branchId = AuthUtils.getBranchId();

        InventoryCategory inventoryCategory =
            inventoryCategoryRepository.findByIdAndBranchId(
                    id,
                    branchId
                )
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INVENTORY_CATEGORY_NOT_FOUND
                    )
                );

        if (request.getCategoryName() != null
            && !request.getCategoryName().equals(inventoryCategory.getCategoryName())
            && inventoryCategoryRepository.existsByBranchIdAndCategoryName(
            branchId,
            request.getCategoryName()
        )) {

            throw new AppException(
                ErrorCode.INVENTORY_CATEGORY_EXISTS
            );
        }

        inventoryCategoryMapper.updateInventoryCategory(
            request,
            inventoryCategory
        );

        inventoryCategory =
            inventoryCategoryRepository.save(inventoryCategory);

        return inventoryCategoryMapper.toInventoryCategoryResponse(
            inventoryCategory
        );
    }

    @Override
    @Transactional
    public InventoryCategoryResponse updateInventoryCategoryStatus(
        String id,
        UpdateInventoryCategoryStatusRequest request
    ) {
        String branchId = AuthUtils.getBranchId();

        InventoryCategory category =
            inventoryCategoryRepository
                .findByIdAndBranchId(id, branchId)
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INVENTORY_CATEGORY_NOT_FOUND
                    )
                );

        category.setStatus(request.getStatus());

        if (request.getStatus() == InventoryCategoryStatus.INACTIVE) {

            inventoryRepository.updateStatusByCategoryIdAndBranchId(
                id,
                branchId,
                InventoryStatus.INACTIVE
            );

        } else {

            inventoryRepository.recalculateStatusByCategoryIdAndBranchId(
                id,
                branchId
            );
        }

        category =
            inventoryCategoryRepository.save(category);

        return inventoryCategoryMapper.toInventoryCategoryResponse(
            category
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryCategoryResponse> getActiveInventoryCategories() {

        String branchId = AuthUtils.getBranchId();

        return inventoryCategoryRepository
            .findByBranchIdAndStatusOrderByCategoryNameAsc(
                branchId,
                InventoryCategoryStatus.ACTIVE
            )
            .stream()
            .map(inventoryCategoryMapper::toInventoryCategoryResponse)
            .toList();
    }
}