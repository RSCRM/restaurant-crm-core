package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.dto.response.TableAreaMapResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableMapResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.mapper.TableMapMapper;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.table.repository.TableAreaRepository;
import com.restaurant.crm.modules.erp.table.service.interfaces.TableMapService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableMapServiceImpl implements TableMapService {

    OrganizationBranchRepository organizationBranchRepository;
    TableAreaRepository tableAreaRepository;
    RestaurantTableRepository restaurantTableRepository;
    TableMapMapper tableMapMapper;

    @Override
    @Transactional(readOnly = true)
    public TableMapResponse getTableMap(String areaId, String requestedBranchId) {
        String branchId = resolveBranch(requestedBranchId);

        List<TableArea> areas;
        List<RestaurantTable> tables;
        if (areaId == null || areaId.isBlank()) {
            areas = tableAreaRepository.findByBranchIdOrderByDisplayOrderAscAreaNameAsc(branchId);
            tables = restaurantTableRepository
                    .findByAreaBranchIdOrderByAreaAreaNameAscTableNumberAsc(branchId);
        } else {
            TableArea area = tableAreaRepository.findByIdAndBranchId(areaId, branchId)
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_AREA_NOT_FOUND));
            areas = List.of(area);
            tables = restaurantTableRepository.findByAreaIdOrderByTableNumberAsc(areaId);
        }

        Map<String, List<RestaurantTable>> tablesByArea = tables.stream()
                .filter(table -> table.getStatus() != RestaurantTableStatus.DELETED)
                .collect(Collectors.groupingBy(table -> table.getArea().getId()));
        List<TableAreaMapResponse> areaResponses = areas.stream()
                .map(area -> toAreaResponse(area, tablesByArea.getOrDefault(area.getId(), List.of())))
                .toList();

        return TableMapResponse.builder()
                .branchId(branchId)
                .areas(areaResponses)
                .build();
    }

    private String resolveBranch(String requestedBranchId) {
        String contextBranchId = AuthUtils.getBranchId();
        String organizationId = AuthUtils.getOrganizationId();
        if (organizationId == null || organizationId.isBlank()) {
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }
        String branchId = contextBranchId == null ? requestedBranchId : contextBranchId;
        if (contextBranchId != null && requestedBranchId != null
                && !contextBranchId.equals(requestedBranchId)) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        if (branchId == null || branchId.isBlank()) {
            return organizationBranchRepository
                    .findByOrganizationIdAndStatus(organizationId, OrganizationBranchStatus.ACTIVE)
                    .stream()
                    .map(OrganizationBranch::getId)
                    .sorted()
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        }
        OrganizationBranch branch = organizationBranchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        if (branch.getStatus() != OrganizationBranchStatus.ACTIVE) {
            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_INACTIVE);
        }
        if (organizationId == null || branch.getOrganization() == null
                || !organizationId.equals(branch.getOrganization().getId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        return branchId;
    }

    private TableAreaMapResponse toAreaResponse(TableArea area, List<RestaurantTable> tables) {
        TableAreaMapResponse response = tableMapMapper.toAreaResponse(area);
        response.setTables(tables.stream().map(tableMapMapper::toTableResponse).toList());
        return response;
    }
}

