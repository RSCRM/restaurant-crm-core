package com.restaurant.crm.modules.licensemanagement.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.licensemanagement.dto.request.CreateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.UpdateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.DeleteLicenseResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseDetailResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.OrganizationSubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.OrganizationSummary;
import com.restaurant.crm.modules.licensemanagement.dto.response.PaginationResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import com.restaurant.crm.modules.licensemanagement.enums.LicenseStatus;
import com.restaurant.crm.modules.licensemanagement.mapper.LicenseMapper;
import com.restaurant.crm.modules.licensemanagement.mapper.LicenseSubscriptionMapper;
import com.restaurant.crm.modules.licensemanagement.repository.LicenseRepository;
import com.restaurant.crm.modules.licensemanagement.repository.LicenseSubscriptionRepository;
import com.restaurant.crm.modules.licensemanagement.service.interfaces.LicenseService;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LicenseServiceImpl implements LicenseService {

    LicenseRepository licenseRepository;
    LicenseSubscriptionRepository subscriptionRepository;
    OrganizationRepository organizationRepository;
    LicenseMapper licenseMapper;
    LicenseSubscriptionMapper licenseSubscriptionMapper;

    @Override
    @Transactional
    public LicenseResponse createLicense(CreateLicenseRequest request) {
        // Check code uniqueness (including soft-deleted records)
        if (licenseRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.LICENSE_CODE_DUPLICATED);
        }

        // Map request to entity
        License license = licenseMapper.toLicense(request);

        // Force status to ACTIVE and deletedAt to null (business rules)
        license.setStatus(LicenseStatus.ACTIVE);
        license.setDeletedAt(null);

        // Save to database
        License savedLicense = licenseRepository.save(license);

        return licenseMapper.toLicenseResponse(savedLicense);
    }

    @Override
    public PagingResponse<LicenseResponse> getLicenses(PagingRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                request.getPageSize(),
                PagingUtil.createSort(request)
        );

        Page<License> licensePage = licenseRepository.findAllByDeletedAtIsNull(pageable);

        return PagingResponse.<LicenseResponse>builder()
                .currentPage(request.getPage())
                .pageSize(licensePage.getSize())
                .totalPages(licensePage.getTotalPages())
                .totalElement(licensePage.getTotalElements())
                .data(licensePage.getContent().stream()
                        .map(licenseMapper::toLicenseResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public LicenseResponse updateLicense(String id, UpdateLicenseRequest request) {
        // Find license (exclude soft-deleted)
        License license = licenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.LICENSE_NOT_FOUND));

        // Check code immutability: if client sent code, it must match current value
        if (request.getCode() != null && !request.getCode().equals(license.getCode())) {
            throw new AppException(ErrorCode.LICENSE_CODE_IMMUTABLE);
        }

        // Update allowed fields only (code, name are NOT updatable)
        license.setDescription(request.getDescription());
        license.setPrice(request.getPrice());
        license.setBillingCycle(request.getBillingCycle());
        license.setMaxBranch(request.getMaxBranch());
        license.setMaxEmployee(request.getMaxEmployee());
        license.setStatus(request.getStatus());

        License savedLicense = licenseRepository.save(license);

        return licenseMapper.toLicenseResponse(savedLicense);
    }

    @Override
    @Transactional
    public DeleteLicenseResponse deleteLicense(String id) {
        // Find license (exclude soft-deleted)
        License license = licenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.LICENSE_NOT_FOUND));

        // Soft delete: set deletedAt to now
        Instant now = Instant.now();
        license.setDeletedAt(now);

        licenseRepository.save(license);

        return DeleteLicenseResponse.builder()
                .id(license.getId())
                .deletedAt(now)
                .build();
    }

    @Override
    @Transactional
    public LicenseResponse lockLicense(String id) {
        License license = licenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.LICENSE_NOT_FOUND));

        if (license.getStatus() == LicenseStatus.LOCKED) {
            throw new AppException(ErrorCode.LICENSE_ALREADY_LOCKED);
        }

        license.setStatus(LicenseStatus.LOCKED);
        License savedLicense = licenseRepository.save(license);
        return licenseMapper.toLicenseResponse(savedLicense);
    }

    @Override
    @Transactional
    public LicenseResponse reactivateLicense(String id) {
        License license = licenseRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.LICENSE_NOT_FOUND));

        if (license.getStatus() == LicenseStatus.ACTIVE) {
            throw new AppException(ErrorCode.LICENSE_ALREADY_ACTIVE);
        }

        license.setStatus(LicenseStatus.ACTIVE);
        License savedLicense = licenseRepository.save(license);
        return licenseMapper.toLicenseResponse(savedLicense);
    }

    @Override
    public LicenseDetailResponse getLicenseDetail(String id, int page, int size) {
        // 1. Find license by id (including soft-deleted — BR-04)
        License license = licenseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LICENSE_NOT_FOUND));

        // 2. Find subscriptions for this license with pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<LicenseSubscription> subscriptionPage = subscriptionRepository.findByLicenseId(id, pageable);

        // 3. Build organization + subscription pairs
        List<OrganizationSubscriptionResponse> organizations = subscriptionPage.getContent().stream()
                .map(sub -> {
                    OrganizationSummary orgSummary = organizationRepository.findById(sub.getOrganizationId())
                            .map(org -> OrganizationSummary.builder()
                                    .id(org.getId())
                                    .name(org.getOrganizationName())
                                    .build())
                            .orElse(OrganizationSummary.builder()
                                    .id(sub.getOrganizationId())
                                    .name("Unknown")
                                    .build());

                    SubscriptionResponse subResponse = licenseSubscriptionMapper.toSubscriptionResponse(sub);

                    return OrganizationSubscriptionResponse.builder()
                            .organization(orgSummary)
                            .subscription(subResponse)
                            .build();
                })
                .toList();

        // 4. Build pagination response
        PaginationResponse pagination = PaginationResponse.builder()
                .page(subscriptionPage.getNumber())
                .size(subscriptionPage.getSize())
                .totalElements(subscriptionPage.getTotalElements())
                .totalPages(subscriptionPage.getTotalPages())
                .build();

        // 5. Build license detail response
        LicenseResponse licenseResponse = licenseMapper.toLicenseResponse(license);

        return LicenseDetailResponse.builder()
                .license(licenseResponse)
                .organizations(organizations)
                .pagination(pagination)
                .build();
    }
}
