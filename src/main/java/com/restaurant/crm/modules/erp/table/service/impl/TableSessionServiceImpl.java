package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.dto.request.TableSessionCreationRequest;
import com.restaurant.crm.modules.erp.table.dto.response.TableSessionResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableSession;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
import com.restaurant.crm.modules.erp.table.mapper.TableSessionMapper;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.table.repository.TableSessionRepository;
import com.restaurant.crm.modules.erp.table.service.interfaces.TableSessionService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableSessionServiceImpl implements TableSessionService {

    OrganizationBranchRepository organizationBranchRepository;
    RestaurantTableRepository restaurantTableRepository;
    TableSessionRepository tableSessionRepository;
    TableSessionMapper tableSessionMapper;

    @Override
    @Transactional
    public TableSessionResponse create(TableSessionCreationRequest request) {
        String branchId = AuthUtils.getBranchId();
        validateBranch(branchId);

        RestaurantTable table = restaurantTableRepository.findByIdForUpdate(request.getTableId())
                .filter(candidate -> branchId.equals(candidate.getArea().getBranchId()))
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_NOT_FOUND));

        if (table.getStatus() != RestaurantTableStatus.AVAILABLE) {
            throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
        }
        if (tableSessionRepository.existsByTableIdAndStatus(table.getId(), TableSessionStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TABLE_SESSION_ACTIVE_EXISTS);
        }
        if (request.getPartySize() > table.getCapacity()) {
            throw new AppException(ErrorCode.TABLE_PARTY_SIZE_EXCEEDS_CAPACITY);
        }

        table.setStatus(RestaurantTableStatus.OCCUPIED);
        restaurantTableRepository.save(table);

        TableSession session = tableSessionRepository.save(TableSession.builder()
                .branchId(branchId)
                .table(table)
                .guestName(request.getGuestName().trim())
                .guestPhone(request.getGuestPhone())
                .partySize(request.getPartySize())
                .status(TableSessionStatus.ACTIVE)
                .startedAt(Instant.now())
                .note(request.getNote())
                .build());
        return tableSessionMapper.toResponse(session);
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
