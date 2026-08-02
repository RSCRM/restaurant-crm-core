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
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.mapper.OrganizationMapper;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationService;
import com.restaurant.crm.modules.erp.organization.specification.OrganizationSpecification;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationServiceImpl implements OrganizationService {

    OrganizationRepository organizationRepository;
    UserRepository userRepository;
    OrganizationMapper organizationMapper;

    @Override
    @Transactional
    public OrganizationResponse createOrganization(
            CreateOrganizationRequest request
    ) {
        if (request.getTaxCode() != null
                && !request.getTaxCode().isBlank()
                && organizationRepository.existsByTaxCode(request.getTaxCode())) {
            throw new AppException(
                    ErrorCode.ORGANIZATION_TAX_CODE_EXISTS
            );
        }

        if (organizationRepository.existsByOwnerId(request.getOwnerId())) {
            throw new AppException(
                    ErrorCode.ORGANIZATION_OWNER_EXISTS
            );
        }

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND)
                );

        Organization organization =
                organizationMapper.toOrganization(request);

        organization.setOwner(owner);

        Organization savedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toOrganizationResponse(
                savedOrganization
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(String id) {
        Organization organization = findOrganizationById(id);

        return organizationMapper.toOrganizationResponse(
                organization
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationByOwnerId(
            String ownerId
    ) {
        Organization organization =
                organizationRepository.findByOwnerId(ownerId)
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.ORGANIZATION_NOT_FOUND
                                )
                        );

        return organizationMapper.toOrganizationResponse(
                organization
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationResponse> getOrganizations(
            int page,
            int size
    ) {
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
        );

        Page<Organization> organizationPage =
                organizationRepository.findAll(pageable);

        return buildPagingResponse(
                organizationPage,
                page
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationResponse> searchOrganizations(
            OrganizationSearchRequest searchRequest,
            PagingRequest pagingRequest
    ) {
        validatePagination(
                pagingRequest.getPage(),
                pagingRequest.getPageSize()
        );

        String organizationId = null;
        String branchId = null;
        String userId = null;

        boolean isAdmin =
                AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE);

        if (!isAdmin) {
            OrgDataScope dataScope = AuthUtils.getDataScope();

            if (dataScope == null) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }

            switch (dataScope) {
                case ORGANIZATION ->
                        organizationId =
                                AuthUtils.getOrganizationId();

                case BRANCH ->
                        branchId =
                                AuthUtils.getBranchId();

                case SELF ->
                        userId =
                                AuthUtils.getCurrentUserId();
            }
        }

        Specification<Organization> specification =
                OrganizationSpecification.build(
                        searchRequest,
                        organizationId,
                        branchId,
                        userId
                );

        Pageable pageable = PageRequest.of(
                pagingRequest.getPage()
                        - GlobalVariableConstant.PAGE_SIZE_INDEX,
                pagingRequest.getPageSize(),
                PagingUtil.createSort(pagingRequest)
        );

        Page<Organization> organizationPage =
                organizationRepository.findAll(
                        specification,
                        pageable
                );

        return buildPagingResponse(
                organizationPage,
                pagingRequest.getPage()
        );
    }

    @Override
    @Transactional
    public OrganizationResponse updateOrganization(
            String id,
            UpdateOrganizationRequest request
    ) {
        Organization organization = findOrganizationById(id);

        validateTaxCodeForUpdate(
                organization,
                request.getTaxCode()
        );

        organizationMapper.updateOrganization(
                request,
                organization
        );

        Organization updatedOrganization =
                organizationRepository.save(organization);

        return organizationMapper.toOrganizationResponse(
                updatedOrganization
        );
    }

    @Override
    @Transactional
    public void deleteOrganization(String id) {
        Organization organization = findOrganizationById(id);

        organizationRepository.delete(organization);
    }

    private Organization findOrganizationById(String id) {
        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(
                                ErrorCode.ORGANIZATION_NOT_FOUND
                        )
                );
    }

    private void validateTaxCodeForUpdate(
            Organization organization,
            String newTaxCode
    ) {
        if (newTaxCode == null || newTaxCode.isBlank()) {
            return;
        }

        String currentTaxCode = organization.getTaxCode();

        boolean taxCodeChanged =
                currentTaxCode == null
                        || !newTaxCode.equals(currentTaxCode);

        if (taxCodeChanged
                && organizationRepository.existsByTaxCode(
                        newTaxCode
                )) {
            throw new AppException(
                    ErrorCode.ORGANIZATION_TAX_CODE_EXISTS
            );
        }
    }

    private void validatePagination(
            int page,
            int pageSize
    ) {
        if (page < GlobalVariableConstant.PAGE_SIZE_INDEX) {
            throw new IllegalArgumentException(
                    "Page không hợp lệ"
            );
        }

        if (pageSize <= 0) {
            throw new IllegalArgumentException(
                    "Page size phải lớn hơn 0"
            );
        }
    }

    private PagingResponse<OrganizationResponse> buildPagingResponse(
            Page<Organization> organizationPage,
            int currentPage
    ) {
        return PagingResponse.<OrganizationResponse>builder()
                .currentPage(currentPage)
                .pageSize(organizationPage.getSize())
                .totalPages(organizationPage.getTotalPages())
                .totalElement(
                        organizationPage.getTotalElements()
                )
                .data(
                        organizationPage.getContent()
                                .stream()
                                .map(
                                        organizationMapper
                                                ::toOrganizationResponse
                                )
                                .toList()
                )
                .build();
    }
}