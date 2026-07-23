package com.restaurant.crm.modules.licensemanagement.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.licensemanagement.dto.request.CreateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.UpdateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.DeleteLicenseResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseResponse;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import com.restaurant.crm.modules.licensemanagement.enums.LicenseStatus;
import com.restaurant.crm.modules.licensemanagement.mapper.LicenseMapper;
import com.restaurant.crm.modules.licensemanagement.repository.LicenseRepository;
import com.restaurant.crm.modules.licensemanagement.service.interfaces.LicenseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LicenseServiceImpl implements LicenseService {

    LicenseRepository licenseRepository;
    LicenseMapper licenseMapper;

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
}
