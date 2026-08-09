package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.organization.dto.request.BranchSearchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationBranchResponse;
import com.restaurant.crm.modules.erp.organization.specification.BranchSpecification;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.mapper.OrganizationBranchMapper;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationBranchService;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationBranchServiceImpl implements OrganizationBranchService {

    OrganizationBranchRepository organizationBranchRepository;
    OrganizationRepository organizationRepository;
    EmployeeRepository employeeRepository;
    OrganizationBranchMapper organizationBranchMapper;

    @Override
    @Transactional
    public OrganizationBranchResponse createOrganizationBranch(
            CreateOrganizationBranchRequest request
    ) {

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.ORGANIZATION_NOT_FOUND));

        if (organizationBranchRepository.existsByOrganizationIdAndBranchName(
                request.getOrganizationId(),
                request.getBranchName())) {

            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_EXISTS);
        }

        OrganizationBranch organizationBranch =
                organizationBranchMapper.toOrganizationBranch(request);

        organizationBranch.setOrganization(organization);

        OrganizationBranch savedOrganizationBranch =
                organizationBranchRepository.save(organizationBranch);

        return toResponse(savedOrganizationBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationBranchResponse getOrganizationBranchById(String id) {

        OrganizationBranch organizationBranch =
                organizationBranchRepository.findById(id)
                        .orElseThrow(() ->
                                new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        return toResponse(organizationBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationBranchResponse> getOrganizationBranches(
        int page,
        int size
    ) {

        Pageable pageable = PageRequest.of(
            page - GlobalVariableConstant.PAGE_SIZE_INDEX,
            size
        );

        Page<OrganizationBranch> organizationBranchPage;

        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {

            organizationBranchPage =
                organizationBranchRepository.findAll(pageable);

        } else {

            switch (AuthUtils.getDataScope()) {

                case ORGANIZATION ->

                    organizationBranchPage =
                        organizationBranchRepository.findByOrganizationId(
                            AuthUtils.getOrganizationId(),
                            pageable
                        );

                case BRANCH ->

                    organizationBranchPage =
                        organizationBranchRepository.findById(
                                AuthUtils.getBranchId()
                            )
                            .map(branch ->
                                new PageImpl<>(
                                    List.of(branch),
                                    pageable,
                                    1
                                )
                            )
                            .orElseThrow(() ->
                                new AppException(
                                    ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND
                                )
                            );

                case SELF -> throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);

                default -> throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
        }

        return PagingResponse.<OrganizationBranchResponse>builder()
            .currentPage(page)
            .pageSize(organizationBranchPage.getSize())
            .totalPages(organizationBranchPage.getTotalPages())
            .totalElement(organizationBranchPage.getTotalElements())
            .data(
                organizationBranchPage.getContent()
                    .stream()
                    .map(this::toResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional
    public OrganizationBranchResponse updateOrganizationBranch(
            String id,
            UpdateOrganizationBranchRequest request
    ) {

        OrganizationBranch organizationBranch =
                organizationBranchRepository.findById(id)
                        .orElseThrow(() ->
                                new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        organizationBranchMapper.updateOrganizationBranch(
                request,
                organizationBranch
        );

        OrganizationBranch updatedOrganizationBranch =
                organizationBranchRepository.save(organizationBranch);

        return toResponse(updatedOrganizationBranch);
    }

    @Override
    @Transactional
    public void deleteOrganizationBranch(String id) {

        OrganizationBranch organizationBranch =
                organizationBranchRepository.findById(id)
                        .orElseThrow(() ->
                                new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        organizationBranchRepository.delete(organizationBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationBranchResponse> searchBranchesByOrgId(
            String orgId, BranchSearchRequest searchRequest, PagingRequest pagingRequest) {

        if (!organizationRepository.existsById(orgId)) {
            throw new AppException(ErrorCode.ORGANIZATION_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(
                pagingRequest.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                pagingRequest.getPageSize(),
                PagingUtil.createSort(pagingRequest)
        );

        Page<OrganizationBranch> branchPage = organizationBranchRepository.findAll(
                BranchSpecification.build(orgId, searchRequest), pageable);

        return PagingResponse.<OrganizationBranchResponse>builder()
                .currentPage(pagingRequest.getPage())
                .pageSize(branchPage.getSize())
                .totalPages(branchPage.getTotalPages())
                .totalElement(branchPage.getTotalElements())
                .data(branchPage.getContent().stream()
                        .map(this::toResponse)
                        .toList())
                .build();
    }

    private OrganizationBranchResponse toResponse(OrganizationBranch branch) {
        OrganizationBranchResponse response = organizationBranchMapper.toOrganizationBranchResponse(branch);
        if (!StringUtils.hasText(branch.getManagerId())) {
            return response;
        }
        employeeRepository.findById(branch.getManagerId()).ifPresent(manager -> populateManager(response, manager));
        return response;
    }

    private void populateManager(OrganizationBranchResponse response, Employee manager) {
        response.setManagerId(manager.getId());
        if (manager.getUser() != null) {
            response.setManagerUserId(manager.getUser().getId());
            response.setManagerName(manager.getUser().getUsername());
            response.setManagerUsername(manager.getUser().getUsername());
            response.setManagerEmail(manager.getUser().getEmail());
        }
    }
}
