package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.dto.response.TableSearchResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.mapper.TableSearchMapper;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.table.service.interfaces.TableSearchService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableSearchServiceImpl implements TableSearchService {

    static final int MAX_PAGE_SIZE = 100;

    OrganizationBranchRepository organizationBranchRepository;
    RestaurantTableRepository restaurantTableRepository;
    TableSearchMapper tableSearchMapper;

    private void validateBranchAccess(String targetBranchId) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)) {
            return;
        }
        String actorUserId = AuthUtils.getCurrentUserId();
        if (AuthUtils.getEmployeeId() == null) {
            organizationBranchRepository.findByIdAndOrganization_OwnerId(targetBranchId, actorUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));
        } else if (!targetBranchId.equals(AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<TableSearchResponse> searchTables(
            String branchId,
            String keyword,
            String areaId,
            RestaurantTableStatus status,
            Integer minCapacity,
            Integer maxCapacity,
            int page,
            int size
    ) {
        validateCriteria(minCapacity, maxCapacity, page, size);
        String activeBranchId = (branchId != null && !branchId.isBlank()) ? branchId : AuthUtils.getBranchId();
        validateBranch(activeBranchId);
        validateBranchAccess(activeBranchId);

        Specification<RestaurantTable> specification =
                (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.join("area", JoinType.INNER).get("branchId"),
                        activeBranchId
                );
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("tableNumber")), pattern));
        }
        if (areaId != null && !areaId.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("area").get("id"), areaId));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }
        if (minCapacity != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
        }
        if (maxCapacity != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("capacity"), maxCapacity));
        }

        PageRequest pageable = PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size,
                Sort.by("area.areaName").ascending().and(Sort.by("tableNumber").ascending())
        );
        Page<RestaurantTable> result = restaurantTableRepository.findAll(specification, pageable);

        return PagingResponse.<TableSearchResponse>builder()
                .currentPage(page)
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElement(result.getTotalElements())
                .data(result.getContent().stream().map(tableSearchMapper::toResponse).toList())
                .build();
    }

    private void validateCriteria(Integer minCapacity, Integer maxCapacity, int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE
                || minCapacity != null && minCapacity < 1
                || maxCapacity != null && maxCapacity < 1
                || minCapacity != null && maxCapacity != null && minCapacity > maxCapacity) {
            throw new AppException(ErrorCode.TABLE_SEARCH_CRITERIA_INVALID);
        }
    }

    private void validateBranch(String branchId) {
        if (branchId == null || branchId.isBlank()) {
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }
        OrganizationBranch branch = organizationBranchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        if (branch.getStatus() != OrganizationBranchStatus.ACTIVE) {
            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_INACTIVE);
        }
    }
}

