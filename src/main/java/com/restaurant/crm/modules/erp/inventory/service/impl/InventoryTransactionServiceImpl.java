package com.restaurant.crm.modules.erp.inventory.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.InventoryTransactionSearchRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryTransactionResponse;
import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import com.restaurant.crm.modules.erp.inventory.entity.InventoryTransaction;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryCategoryStatus;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
import com.restaurant.crm.modules.erp.inventory.mapper.InventoryTransactionMapper;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryRepository;
import com.restaurant.crm.modules.erp.inventory.repository.InventoryTransactionRepository;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryTransactionService;
import com.restaurant.crm.modules.erp.inventory.specification.InventoryTransactionSpecification;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
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
    EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public InventoryTransactionResponse createTransaction(
        CreateInventoryTransactionRequest request
    ) {

        String branchId = AuthUtils.getBranchId();
        String employeeId = AuthUtils.getEmployeeId();

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (!request.getTransactionType().isValidDirection(request.getTransactionDirection())) {
            throw new AppException(ErrorCode.INVENTORY_INVALID_TRANSACTION_TYPE_DIRECTION);
        }

        Inventory inventory =
            inventoryRepository.findByIdAndBranchId(
                    request.getInventoryId(),
                    branchId
                )
                .orElseThrow(() ->
                    new AppException(ErrorCode.INVENTORY_NOT_FOUND)
                );
        if (inventory.getStatus() == InventoryStatus.INACTIVE) {
            throw new AppException(ErrorCode.INVENTORY_INACTIVE);
        }
        InventoryTransaction transaction =
            transactionMapper.toInventoryTransaction(request);

        transaction.setInventory(inventory);
        transaction.setEmployee(employee);
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

        if (direction == InventoryTransactionDirection.OUT
            && inventory.getQuantity().compareTo(quantity) < 0) {
            throw new AppException(ErrorCode.INVENTORY_INSUFFICIENT_STOCK);
        }

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

        inventory.setStatus(
            calculateStatus(
                inventory.getQuantity(),
                inventory.getMinimumQuantity()
            )
        );

        inventoryRepository.save(inventory);
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
    public InventoryTransactionResponse getTransactionById(
        String id
    ) {
        String branchId = AuthUtils.getBranchId();

        return transactionRepository.findByIdAndInventoryBranchId(
                id,
                branchId
            )
            .map(transactionMapper::toInventoryTransactionResponse)
            .orElseThrow(() ->
                new AppException(
                    ErrorCode.INVENTORY_TRANSACTION_NOT_FOUND
                )
            );
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<InventoryTransactionResponse> getTransactionsByBranch(
        int page,
        int size
    ) {
        String branchId = AuthUtils.getBranchId();

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<InventoryTransaction> result =
            transactionRepository.findByInventoryBranchId(
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
    public PagingResponse<InventoryTransactionResponse> searchTransactions(
        InventoryTransactionSearchRequest searchRequest,
        PagingRequest pagingRequest
    ) {

        String branchId = AuthUtils.getBranchId();
        Sort sort = Sort.by(Sort.Direction.DESC, "transactionTime");

        Pageable pageable = PageRequest.of(
            pagingRequest.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
            pagingRequest.getPageSize(),
            sort
        );

        Page<InventoryTransaction> transactionPage =
            transactionRepository.findAll(
                InventoryTransactionSpecification.build(
                    branchId,
                    searchRequest
                ),
                pageable
            );

        return PagingResponse.<InventoryTransactionResponse>builder()
            .currentPage(pagingRequest.getPage())
            .pageSize(transactionPage.getSize())
            .totalPages(transactionPage.getTotalPages())
            .totalElement(transactionPage.getTotalElements())
            .data(
                transactionPage.getContent()
                    .stream()
                    .map(transactionMapper::toInventoryTransactionResponse)
                    .toList()
            )
            .build();
    }
}