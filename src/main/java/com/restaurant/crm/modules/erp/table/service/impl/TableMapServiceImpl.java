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
    public TableMapResponse getTableMap(String areaId) {
        String branchId = AuthUtils.getBranchId();
        validateBranch(branchId);

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
                .collect(Collectors.groupingBy(table -> table.getArea().getId()));
        List<TableAreaMapResponse> areaResponses = areas.stream()
                .map(area -> toAreaResponse(area, tablesByArea.getOrDefault(area.getId(), List.of())))
                .toList();

        return TableMapResponse.builder()
                .branchId(branchId)
                .areas(areaResponses)
                .build();
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

    private TableAreaMapResponse toAreaResponse(TableArea area, List<RestaurantTable> tables) {
        TableAreaMapResponse response = tableMapMapper.toAreaResponse(area);
        response.setTables(tables.stream().map(tableMapMapper::toTableResponse).toList());
        return response;
    }
}

