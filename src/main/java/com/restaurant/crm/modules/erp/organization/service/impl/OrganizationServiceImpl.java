package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.OrganizationSearchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationResponse;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.mapper.OrganizationMapper;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationService;
import com.restaurant.crm.modules.erp.organization.specification.OrganizationSpecification;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationServiceImpl implements OrganizationService {

    OrganizationRepository organizationRepository;
    OrganizationBranchRepository organizationBranchRepository;
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

        Pageable pageable = PageRequest.of(
            page - GlobalVariableConstant.PAGE_SIZE_INDEX,
            size
        );

        Page<Organization> organizationPage;

        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {

            organizationPage = organizationRepository.findAll(pageable);

        } else {

            OrgDataScope dataScope = AuthUtils.getDataScope();

            switch (dataScope) {

                case ORGANIZATION ->

                    organizationPage = organizationRepository.findById(
                            AuthUtils.getOrganizationId()
                        )
                        .map(organization ->
                            new PageImpl<>(
                                List.of(organization),
                                pageable,
                                1
                            )
                        )
                        .orElseThrow(() ->
                            new AppException(
                                ErrorCode.ORGANIZATION_NOT_FOUND
                            )
                        );

                case BRANCH -> {

                    OrganizationBranch branch =
                        organizationBranchRepository.findById(
                                AuthUtils.getBranchId()
                            )
                            .orElseThrow(() ->
                                new AppException(
                                    ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND
                                )
                            );

                    organizationPage =
                        new PageImpl<>(
                            List.of(branch.getOrganization()),
                            pageable,
                            1
                        );
                }

                case SELF ->

                    organizationPage = organizationRepository.findByOwnerId(
                        AuthUtils.getCurrentUserId(),
                        pageable
                    );

                default ->
                    throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
        }

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
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationResponse> searchOrganizations(
            OrganizationSearchRequest searchRequest, PagingRequest pagingRequest) {

        // Resolve data scope from JWT
        String orgId = null;
        String branchId = null;
        String userId = null;

        if (!AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {
            OrgDataScope dataScope = AuthUtils.getDataScope();
            switch (dataScope) {
                case ORGANIZATION -> orgId = AuthUtils.getOrganizationId();
                case BRANCH -> branchId = AuthUtils.getBranchId();
                case SELF -> userId = AuthUtils.getCurrentUserId();
            }
        }

        Specification<Organization> spec =
                OrganizationSpecification.build(searchRequest, orgId, branchId, userId);

        Pageable pageable = PageRequest.of(
                pagingRequest.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                pagingRequest.getPageSize(),
                PagingUtil.createSort(pagingRequest)
        );

        Page<Organization> organizationPage = organizationRepository.findAll(spec, pageable);

        return PagingResponse.<OrganizationResponse>builder()
                .currentPage(pagingRequest.getPage())
                .pageSize(organizationPage.getSize())
                .totalPages(organizationPage.getTotalPages())
                .totalElement(organizationPage.getTotalElements())
                .data(organizationPage.getContent().stream()
                        .map(organizationMapper::toOrganizationResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationResponse> searchOrganizationsWithoutActiveSubscription(
            OrganizationSearchRequest searchRequest, PagingRequest pagingRequest) {

        // Resolve data scope from JWT
        String orgId = null;
        String branchId = null;
        String userId = null;

        if (!AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {
            try {
                OrgDataScope dataScope = AuthUtils.getDataScope();
                switch (dataScope) {
                    case ORGANIZATION -> orgId = AuthUtils.getOrganizationId();
                    case BRANCH -> branchId = AuthUtils.getBranchId();
                    case SELF -> userId = AuthUtils.getCurrentUserId();
                }
            } catch (AppException e) {
                // Identity Token without context claims — no data scope filtering
            }
        }

        Specification<Organization> spec =
                OrganizationSpecification.buildWithoutActiveSubscription(searchRequest, orgId, branchId, userId);

        Pageable pageable = PageRequest.of(
                pagingRequest.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                pagingRequest.getPageSize(),
                PagingUtil.createSort(pagingRequest)
        );

        Page<Organization> organizationPage = organizationRepository.findAll(spec, pageable);

        return PagingResponse.<OrganizationResponse>builder()
                .currentPage(pagingRequest.getPage())
                .pageSize(organizationPage.getSize())
                .totalPages(organizationPage.getTotalPages())
                .totalElement(organizationPage.getTotalElements())
                .data(organizationPage.getContent().stream()
                        .map(organizationMapper::toOrganizationResponse)
                        .toList())
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
