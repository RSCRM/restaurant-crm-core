package com.restaurant.crm.modules.erp.inventory.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryResponse;
import com.restaurant.crm.modules.erp.inventory.entity.Ingredient;
import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import com.restaurant.crm.modules.erp.inventory.mapper.InventoryMapper;
import com.restaurant.crm.modules.erp.inventory.repository.IngredientRepository;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryRepository;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryService;
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
    IngredientRepository ingredientRepository;
    InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse createInventory(
        CreateInventoryRequest request
    ) {

        if (inventoryRepository.existsByIngredientId(
            request.getIngredientId()
        )) {
            throw new AppException(
                ErrorCode.INVENTORY_EXISTS
            );
        }

        Ingredient ingredient =
            ingredientRepository.findById(
                    request.getIngredientId()
                )
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INGREDIENT_NOT_FOUND
                    )
                );

        Inventory inventory =
            inventoryMapper.toInventory(request);

        inventory.setIngredient(ingredient);

        inventory.setStatus(
            calculateStatus(
                inventory.getQuantity(),
                inventory.getMinimumQuantity()
            )
        );

        Inventory saved =
            inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(String id) {

        Inventory inventory =
            inventoryRepository.findById(id)
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INVENTORY_NOT_FOUND
                    )
                );

        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByIngredientId(
        String ingredientId
    ) {

        Inventory inventory =
            inventoryRepository.findByIngredientId(ingredientId)
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INVENTORY_NOT_FOUND
                    )
                );

        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryResponse> getInventoriesByBranch(
        String branchId,
        int page,
        int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<Inventory> inventoryPage =
            inventoryRepository.findByIngredientBranchId(
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

        Inventory inventory =
            inventoryRepository.findById(id)
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INVENTORY_NOT_FOUND
                    )
                );

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

        Inventory updated =
            inventoryRepository.save(inventory);

        return inventoryMapper.toInventoryResponse(updated);
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

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryResponse> getInventoriesByStatus(
        String branchId,
        InventoryStatus status,
        int page,
        int size
    ) {

        Pageable pageable =
            PageRequest.of(page - GlobalVariableConstant.PAGE_SIZE_INDEX, size);

        Page<Inventory> inventoryPage =
            inventoryRepository.findByIngredientBranchIdAndStatus(
                branchId,
                status,
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
}
