package com.restaurant.crm.modules.erp.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.dto.response.LicenseInfoResponse;
import com.restaurant.crm.modules.erp.entity.LicenseContract;
import com.restaurant.crm.modules.erp.mapper.LicenseContractMapper;
import com.restaurant.crm.modules.erp.repository.LicenseContractRepository;
import com.restaurant.crm.modules.erp.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.service.interfaces.ContractService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractServiceImpl implements ContractService {

    LicenseContractRepository licenseContractRepository;
    OrganizationBranchRepository branchRepository;
    LicenseContractMapper licenseContractMapper;

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<LicenseInfoResponse> getMyLicenseInfos(PagingRequest request) {
        String ownerId = getCurrentOwnerId();
        Pageable pageable = PageRequest.of(
                request.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                request.getPageSize(),
                PagingUtil.createSort(request)
        );

        Page<LicenseContract> licensePage = licenseContractRepository.findAllByOrganization_Owner_Id(
                ownerId,
                pageable
        );

        return PagingResponse.<LicenseInfoResponse>builder()
                .currentPage(request.getPage())
                .pageSize(licensePage.getSize())
                .totalPages(licensePage.getTotalPages())
                .totalElement(licensePage.getTotalElements())
                .data(licensePage.getContent().stream()
                        .map(this::toResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LicenseInfoResponse getLicenseInfoByOrganization(String organizationId) {
        LicenseContract licenseContract = licenseContractRepository
                .findFirstByOrganization_IdAndOrganization_Owner_IdOrderByCreatedAtDesc(
                        organizationId,
                        getCurrentOwnerId())
                .orElseThrow(() -> new AppException(ErrorCode.LICENSE_NOT_FOUND));

        return toResponse(licenseContract);
    }

    private LicenseInfoResponse toResponse(LicenseContract licenseContract) {
        LicenseInfoResponse response = licenseContractMapper.toLicenseInfoResponse(licenseContract);
        response.setUsedBranches(branchRepository.countByOrganization_Id(
                licenseContract.getOrganization().getId()
        ));
        return response;
    }

    private String getCurrentOwnerId() {
        String ownerId = AuthUtils.getCurrentUserId();
        if (!StringUtils.hasText(ownerId)) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
        return ownerId;
    }
}
