package com.restaurant.crm.modules.licensemanagement.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.licensemanagement.dto.request.GrantSubscriptionRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import com.restaurant.crm.modules.licensemanagement.enums.LicenseStatus;
import com.restaurant.crm.modules.licensemanagement.enums.SubscriptionStatus;
import com.restaurant.crm.modules.licensemanagement.mapper.LicenseSubscriptionMapper;
import com.restaurant.crm.modules.licensemanagement.repository.LicenseRepository;
import com.restaurant.crm.modules.licensemanagement.repository.LicenseSubscriptionRepository;
import com.restaurant.crm.modules.licensemanagement.service.interfaces.LicenseSubscriptionService;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
            return licenseSubscriptionMapper.toSubscriptionResponse(saved);
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

        return licenseSubscriptionMapper.toSubscriptionResponse(saved);
    }
}
