package com.restaurant.crm.modules.licensemanagement.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.licensemanagement.dto.request.GrantSubscriptionRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.SubscriptionSearchRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import com.restaurant.crm.modules.licensemanagement.enums.LicenseStatus;
import com.restaurant.crm.modules.licensemanagement.enums.SubscriptionStatus;
import com.restaurant.crm.modules.licensemanagement.mapper.LicenseSubscriptionMapper;
import com.restaurant.crm.modules.licensemanagement.repository.LicenseRepository;
import com.restaurant.crm.modules.licensemanagement.repository.LicenseSubscriptionRepository;
import com.restaurant.crm.modules.licensemanagement.service.interfaces.LicenseSubscriptionService;
import com.restaurant.crm.modules.licensemanagement.specification.LicenseSubscriptionSpecification;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LicenseSubscriptionServiceImpl implements LicenseSubscriptionService {

    LicenseSubscriptionRepository subscriptionRepository;
    LicenseRepository licenseRepository;
    OrganizationRepository organizationRepository;
    LicenseSubscriptionMapper licenseSubscriptionMapper;

    @Override
    @Transactional
    public SubscriptionResponse grantSubscription(GrantSubscriptionRequest request) {
        // 1. Check Organization exists
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));

        // 2. Check License exists and is not soft-deleted
        License license = licenseRepository.findByIdAndDeletedAtIsNull(request.getLicenseId())
                .orElseThrow(() -> new AppException(ErrorCode.LICENSE_NOT_FOUND));

        // 3. Check License is ACTIVE (not LOCKED)
        if (license.getStatus() == LicenseStatus.LOCKED) {
            throw new AppException(ErrorCode.LICENSE_LOCKED_CANNOT_ISSUE);
        }

        // 4. Check Organization doesn't already have an ACTIVE subscription
        if (subscriptionRepository.existsByOrganizationIdAndStatus(
                request.getOrganizationId(), SubscriptionStatus.ACTIVE)) {
            throw new AppException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }

        // 5. Calculate dates
        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();
        int cycleDays = license.getBillingCycle().getDays();
        LocalDate endDate = startDate.plusDays(cycleDays);

        // 6. Build subscription with snapshot from License
        LicenseSubscription subscription = LicenseSubscription.builder()
                .licenseId(license.getId())
                .organizationId(organization.getId())
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .price(license.getPrice())
                .billingCycle(license.getBillingCycle())
                .maxBranch(license.getMaxBranch())
                .maxEmployee(license.getMaxEmployee())
                .build();

        // 7. Save (catch race condition via Unique Index)
        try {
            LicenseSubscription saved = subscriptionRepository.save(subscription);
            return licenseSubscriptionMapper.toSubscriptionResponse(saved, license);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.ACTIVE_SUBSCRIPTION_EXISTS);
        }
    }

    @Override
    @Transactional
    public SubscriptionResponse renewSubscription(String id) {
        LicenseSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getStatus() == SubscriptionStatus.REVOKED) {
            throw new AppException(ErrorCode.SUBSCRIPTION_ALREADY_REVOKED);
        }

        LocalDate today = LocalDate.now();
        int cycleDays = subscription.getBillingCycle().getDays();

        if (!subscription.getEndDate().isBefore(today)) {
            subscription.setEndDate(subscription.getEndDate().plusDays(cycleDays));
        } else {
            subscription.setEndDate(today.plusDays(cycleDays));
        }

        if (subscription.getStatus() == SubscriptionStatus.EXPIRED) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        LicenseSubscription saved = subscriptionRepository.save(subscription);

        License license = licenseRepository.findById(saved.getLicenseId())
                .orElse(null);
        return licenseSubscriptionMapper.toSubscriptionResponse(saved, license);
    }

    @Override
    @Transactional
    public SubscriptionResponse revokeSubscription(String id) {
        LicenseSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getStatus() == SubscriptionStatus.REVOKED) {
            throw new AppException(ErrorCode.SUBSCRIPTION_ALREADY_REVOKED);
        }

        subscription.setStatus(SubscriptionStatus.REVOKED);
        LicenseSubscription saved = subscriptionRepository.save(subscription);

        License license = licenseRepository.findById(saved.getLicenseId())
                .orElse(null);
        return licenseSubscriptionMapper.toSubscriptionResponse(saved, license);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<SubscriptionResponse> searchSubscriptionsByOrganization(
            String organizationId, SubscriptionSearchRequest searchRequest, PagingRequest pagingRequest) {

        Pageable pageable = PageRequest.of(
                pagingRequest.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                pagingRequest.getPageSize(),
                PagingUtil.createSort(pagingRequest)
        );

        Page<LicenseSubscription> subscriptionPage = subscriptionRepository.findAll(
                LicenseSubscriptionSpecification.build(organizationId, searchRequest), pageable);

        // Batch fetch licenses for the page
        List<String> licenseIds = subscriptionPage.getContent().stream()
                .map(LicenseSubscription::getLicenseId)
                .distinct()
                .toList();
        Map<String, License> licenseMap = licenseRepository.findByIdInAndDeletedAtIsNull(licenseIds).stream()
                .collect(Collectors.toMap(License::getId, l -> l));

        return PagingResponse.<SubscriptionResponse>builder()
                .currentPage(pagingRequest.getPage())
                .pageSize(subscriptionPage.getSize())
                .totalPages(subscriptionPage.getTotalPages())
                .totalElement(subscriptionPage.getTotalElements())
                .data(subscriptionPage.getContent().stream()
                        .map(sub -> licenseSubscriptionMapper.toSubscriptionResponse(
                                sub, licenseMap.get(sub.getLicenseId())))
                        .toList())
                .build();
    }
}
