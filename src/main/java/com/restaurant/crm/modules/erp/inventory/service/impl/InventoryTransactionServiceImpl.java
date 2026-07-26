package com.restaurant.crm.modules.erp.inventory.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryTransactionResponse;
import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import com.restaurant.crm.modules.erp.inventory.entity.InventoryTransaction;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
import com.restaurant.crm.modules.erp.inventory.mapper.InventoryTransactionMapper;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryRepository;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryTransactionRepository;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryTransactionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionServiceImpl implements InventoryTransactionService {
    InventoryTransactionRepository transactionRepository;
    InventoryRepository inventoryRepository;
    InventoryTransactionMapper transactionMapper;

    @Override
    @Transactional
    public InventoryTransactionResponse createTransaction(
        CreateInventoryTransactionRequest request
    ) {

        Inventory inventory =
            inventoryRepository.findById(
                    request.getInventoryId()
                )
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INVENTORY_NOT_FOUND
                    )
                );

        InventoryTransaction transaction =
            transactionMapper.toInventoryTransaction(request);

        transaction.setInventory(inventory);

        transaction.setTransactionTime(
            Instant.now()
        );

        updateInventoryQuantity(
            inventory,
            request.getTransactionDirection(),
            request.getQuantity()
        );

        InventoryTransaction saved =
            transactionRepository.save(transaction);


        return transactionMapper.toInventoryTransactionResponse(saved);
    }

    private void updateInventoryQuantity(
        Inventory inventory,
        InventoryTransactionDirection direction,
        BigDecimal quantity
    ) {

        if(direction == InventoryTransactionDirection.IN) {

            inventory.setQuantity(
                inventory.getQuantity()
                    .add(quantity)
            );

        } else {

            inventory.setQuantity(
                inventory.getQuantity()
                    .subtract(quantity)
            );
        }

        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryTransactionResponse getTransactionById(
        String id
    ) {

        return transactionRepository.findById(id)
            .map(transactionMapper::toInventoryTransactionResponse)
            .orElseThrow(() ->
                new AppException(
                    ErrorCode.INVENTORY_TRANSACTION_NOT_FOUND
                )
            );
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryTransactionResponse> getTransactionsByInventory(
        String inventoryId,
        int page,
        int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<InventoryTransaction> result =
            transactionRepository.findByInventoryId(
                inventoryId,
                pageable
            );

        return PagingResponse.<InventoryTransactionResponse>builder()
            .currentPage(page)
            .pageSize(result.getSize())
            .totalPages(result.getTotalPages())
            .totalElement(result.getTotalElements())
            .data(
                result.getContent()
                    .stream()
                    .map(transactionMapper::toInventoryTransactionResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryTransactionResponse> getTransactionsByBranch(
        String branchId,
        int page,
        int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<InventoryTransaction> result =
            transactionRepository.findByInventoryIngredientBranchId(
                branchId,
                pageable
            );

        return PagingResponse.<InventoryTransactionResponse>builder()
            .currentPage(page)
            .pageSize(result.getSize())
            .totalPages(result.getTotalPages())
            .totalElement(result.getTotalElements())
            .data(
                result.getContent()
                    .stream()
                    .map(transactionMapper::toInventoryTransactionResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryTransactionResponse> getTransactionsByType(
        String branchId,
        InventoryTransactionType type,
        int page,
        int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<InventoryTransaction> transactionPage =
            transactionRepository
                .findByInventoryIngredientBranchIdAndTransactionType(
                    branchId,
                    type,
                    pageable
                );

        return PagingResponse.<InventoryTransactionResponse>builder()
            .currentPage(page)
            .pageSize(transactionPage.getSize())
            .totalPages(transactionPage.getTotalPages())
            .totalElement(transactionPage.getTotalElements())
            .data(
                transactionPage.getContent()
                    .stream()
                    .map(
                        transactionMapper::toInventoryTransactionResponse
                    )
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryTransactionResponse> getTransactionsByDateRange(
        String branchId,
        Instant from,
        Instant to,
        int page,
        int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<InventoryTransaction> transactionPage =
            transactionRepository
                .findByInventoryIngredientBranchIdAndTransactionTimeBetween(
                    branchId,
                    from,
                    to,
                    pageable
                );

        return PagingResponse.<InventoryTransactionResponse>builder()
            .currentPage(page)
            .pageSize(transactionPage.getSize())
            .totalPages(transactionPage.getTotalPages())
            .totalElement(transactionPage.getTotalElements())
            .data(
                transactionPage.getContent()
                    .stream()
                    .map(
                        transactionMapper::toInventoryTransactionResponse
                    )
                    .toList()
            )
            .build();
    }
}