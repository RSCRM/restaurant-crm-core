package com.restaurant.crm.modules.erp.inventory.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.InventorySearchRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryResponse;
import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import com.restaurant.crm.modules.erp.inventory.entity.InventoryCategory;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import com.restaurant.crm.modules.erp.inventory.mapper.InventoryMapper;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryCategoryRepository;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryRepository;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryService;
import com.restaurant.crm.modules.erp.inventory.specification.InventorySpecification;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;
    InventoryCategoryRepository inventoryCategoryRepository;
    OrganizationBranchRepository organizationBranchRepository;
    InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse createInventory(
        CreateInventoryRequest request
    ) {

        String branchId = AuthUtils.getBranchId();

        OrganizationBranch branch =
            organizationBranchRepository.findById(branchId)
                .orElseThrow(() ->
                    new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        InventoryCategory category =
            inventoryCategoryRepository
                .findByIdAndBranchId(
                    request.getInventoryCategoryId(),
                    branchId
                )
                .orElseThrow(() ->
                    new AppException(ErrorCode.INVENTORY_CATEGORY_NOT_FOUND));

        if (inventoryRepository.existsByBranchIdAndInventoryName(
            branchId,
            request.getInventoryName()
        )) {
            throw new AppException(ErrorCode.INVENTORY_EXISTS);
        }

        Inventory inventory =
            inventoryMapper.toInventory(request);

        inventory.setBranch(branch);
        inventory.setInventoryCategory(category);

        inventory.setStatus(
            calculateStatus(
                inventory.getQuantity(),
                inventory.getMinimumQuantity()
            )
        );

        inventory =
            inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(
        String id
    ) {

        String branchId = AuthUtils.getBranchId();

        Inventory inventory =
            inventoryRepository
                .findByIdAndBranchId(id, branchId)
                .orElseThrow(() ->
                    new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryResponse> getInventoriesByBranch(
        int page,
        int size
    ) {

        String branchId = AuthUtils.getBranchId();

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<Inventory> inventoryPage =
            inventoryRepository.findByBranchId(
                branchId,
                pageable
            );

        return PagingResponse.<InventoryResponse>builder()
            .currentPage(page)
            .pageSize(inventoryPage.getSize())
            .totalPages(inventoryPage.getTotalPages())
            .totalElement(inventoryPage.getTotalElements())
            .data(
                inventoryPage.getContent()
                    .stream()
                    .map(inventoryMapper::toInventoryResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(
        String id,
        UpdateInventoryRequest request
    ) {

        String branchId = AuthUtils.getBranchId();

        Inventory inventory =
            inventoryRepository
                .findByIdAndBranchId(id, branchId)
                .orElseThrow(() ->
                    new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        if (request.getInventoryCategoryId() != null) {

            InventoryCategory category =
                inventoryCategoryRepository
                    .findByIdAndBranchId(
                        request.getInventoryCategoryId(),
                        branchId
                    )
                    .orElseThrow(() ->
                        new AppException(ErrorCode.INVENTORY_CATEGORY_NOT_FOUND));

            inventory.setInventoryCategory(category);
        }

        if (request.getInventoryName() != null
            && !request.getInventoryName().equals(inventory.getInventoryName())
            && inventoryRepository.existsByBranchIdAndInventoryName(
            branchId,
            request.getInventoryName()
        )) {

            throw new AppException(ErrorCode.INVENTORY_EXISTS);
        }

        inventoryMapper.updateInventory(
            request,
            inventory
        );

        inventory.setStatus(
            calculateStatus(
                inventory.getQuantity(),
                inventory.getMinimumQuantity()
            )
        );

        inventory =
            inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Transactional
    public void deleteInventory(
        String id
    ) {

        String branchId = AuthUtils.getBranchId();

        Inventory inventory =
            inventoryRepository
                .findByIdAndBranchId(id, branchId)
                .orElseThrow(() ->
                    new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        inventoryRepository.delete(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryResponse> getInventoriesByCategory(
        String categoryId,
        int page,
        int size
    ) {
        String branchId = AuthUtils.getBranchId();

        Pageable pageable = PageRequest.of(
            page - GlobalVariableConstant.PAGE_SIZE_INDEX,
            size
        );

        Page<Inventory> inventoryPage =
            inventoryRepository.findByInventoryCategoryIdAndBranchId(
                categoryId,
                branchId,
                pageable
            );

        return PagingResponse.<InventoryResponse>builder()
            .currentPage(page)
            .pageSize(inventoryPage.getSize())
            .totalPages(inventoryPage.getTotalPages())
            .totalElement(inventoryPage.getTotalElements())
            .data(
                inventoryPage.getContent()
                    .stream()
                    .map(inventoryMapper::toInventoryResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryResponse> searchInventories(
        InventorySearchRequest searchRequest,
        PagingRequest pagingRequest
    ) {

        String branchId = AuthUtils.getBranchId();

        Pageable pageable =
            PageRequest.of(
                pagingRequest.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                pagingRequest.getPageSize(),
                PagingUtil.createSort(pagingRequest)
            );

        Page<Inventory> inventoryPage =
            inventoryRepository.findAll(
                InventorySpecification.build(
                    branchId,
                    searchRequest
                ),
                pageable
            );

        return PagingResponse.<InventoryResponse>builder()
            .currentPage(pagingRequest.getPage())
            .pageSize(inventoryPage.getSize())
            .totalPages(inventoryPage.getTotalPages())
            .totalElement(inventoryPage.getTotalElements())
            .data(
                inventoryPage.getContent()
                    .stream()
                    .map(inventoryMapper::toInventoryResponse)
                    .toList()
            )
            .build();
    }

    private InventoryStatus calculateStatus(
        BigDecimal quantity,
        BigDecimal minimumQuantity
    ) {

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return InventoryStatus.OUT_OF_STOCK;
        }

        if (quantity.compareTo(minimumQuantity) <= 0) {
            return InventoryStatus.LOW;
        }

        return InventoryStatus.GOOD;
    }
}