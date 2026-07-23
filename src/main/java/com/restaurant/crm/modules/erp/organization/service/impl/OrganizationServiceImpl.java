package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationResponse;
import com.restaurant.crm.modules.erp.shared.entity.Organization;
import com.restaurant.crm.modules.erp.organization.mapper.OrganizationMapper;
import com.restaurant.crm.modules.erp.shared.repository.OrganizationRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationServiceImpl implements OrganizationService {

    OrganizationRepository organizationRepository;

    OrganizationMapper organizationMapper;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {

        if (organizationRepository.existsByOwnerId(request.getOwnerId())) {
            throw new AppException(ErrorCode.ORGANIZATION_EXISTS);
        }

        if (request.getTaxCode() != null
                && organizationRepository.existsByTaxCode(request.getTaxCode())) {
            throw new AppException(ErrorCode.ORGANIZATION_TAX_CODE_EXISTS);
        }

        Organization organization =
                organizationMapper.toOrganization(request);

        organization = organizationRepository.save(organization);

        return organizationMapper.toOrganizationResponse(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(String id) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));

        return organizationMapper.toOrganizationResponse(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationByOwnerId(String ownerId) {

        Organization organization = organizationRepository.findByOwnerId(ownerId)
                .orElseThrow(() ->
                        new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));

        return organizationMapper.toOrganizationResponse(organization);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationResponse> getOrganizations(
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page - GlobalVariableConstant.PAGE_SIZE_INDEX, size);

        Page<Organization> organizationPage =
                organizationRepository.findAll(pageable);

        return PagingResponse.<OrganizationResponse>builder()
                .currentPage(page)
                .pageSize(organizationPage.getSize())
                .totalPages(organizationPage.getTotalPages())
                .totalElement(organizationPage.getTotalElements())
                .data(
                        organizationPage.getContent()
                                .stream()
                                .map(organizationMapper::toOrganizationResponse)
                                .toList()
                )
                .build();
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(
            String id,
            UpdateOrganizationRequest request
    ) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));

        if (request.getTaxCode() != null
                && !request.getTaxCode().equals(organization.getTaxCode())
                && organizationRepository.existsByTaxCode(request.getTaxCode())) {

            throw new AppException(
                    ErrorCode.ORGANIZATION_TAX_CODE_EXISTS
            );
        }

        organizationMapper.updateOrganization(
                request,
                organization
        );

        organization = organizationRepository.save(organization);

        return organizationMapper.toOrganizationResponse(organization);
    }

    @Override
    @Transactional
    public void deleteOrganization(String id) {

        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));

        organizationRepository.delete(organization);
    }
}
