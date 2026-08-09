package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationBranchResponse;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
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
        validateOrganizationAccess(request.getOrganizationId());

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
        validateBranchAccess(organizationBranch);

        return toResponse(organizationBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationBranchResponse> getOrganizationBranches(
        int page,
        int size,
        String keyword,
        OrganizationBranchStatus status
    ) {

        Pageable pageable = PageRequest.of(
            page - GlobalVariableConstant.PAGE_SIZE_INDEX,
            size
        );
        String normalizedKeyword = normalizeKeyword(keyword);
        String keywordPattern = toKeywordPattern(normalizedKeyword);

        Page<OrganizationBranch> organizationBranchPage;

        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {

            organizationBranchPage =
                findBranches(null, keywordPattern, status, pageable);

        } else {

            switch (AuthUtils.getDataScope()) {

                case ORGANIZATION ->

                    organizationBranchPage =
                        findBranches(
                            AuthUtils.getOrganizationId(),
                            keywordPattern,
                            status,
                            pageable
                        );

                case BRANCH ->

                    organizationBranchPage =
                        organizationBranchRepository.findById(
                                AuthUtils.getBranchId()
                            )
                            .map(branch ->
                                new PageImpl<>(
                                    matchesFilters(branch, normalizedKeyword, status)
                                        ? List.of(branch)
                                        : List.of(),
                                    pageable,
                                    matchesFilters(branch, normalizedKeyword, status) ? 1 : 0
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

        return toPagingResponse(page, organizationBranchPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationBranchResponse> getOrganizationBranchesByOrganization(
            String organizationId,
            int page,
            int size,
            String keyword,
            OrganizationBranchStatus status
    ) {
        validateOrganizationAccess(organizationId);

        Pageable pageable = PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
        );
        String normalizedKeyword = normalizeKeyword(keyword);
        String keywordPattern = toKeywordPattern(normalizedKeyword);

        Page<OrganizationBranch> organizationBranchPage;
        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)
                || AuthUtils.getDataScope() == OrgDataScope.ORGANIZATION) {
            organizationBranchPage = findBranches(
                    organizationId,
                    keywordPattern,
                    status,
                    pageable
            );
        } else {
            organizationBranchPage =
                    organizationBranchRepository.findById(AuthUtils.getBranchId())
                            .filter(branch -> organizationId.equals(branch.getOrganization().getId()))
                            .map(branch -> {
                                boolean matches = matchesFilters(branch, normalizedKeyword, status);
                                return new PageImpl<>(
                                        matches ? List.of(branch) : List.of(),
                                        pageable,
                                        matches ? 1 : 0
                                );
                            })
                            .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        }

        return toPagingResponse(page, organizationBranchPage);
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
        validateBranchAccess(organizationBranch);

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
        validateBranchAccess(organizationBranch);

        organizationBranchRepository.delete(organizationBranch);
    }

    private PagingResponse<OrganizationBranchResponse> toPagingResponse(
            int page,
            Page<OrganizationBranch> organizationBranchPage
    ) {
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

    private OrganizationBranchResponse toResponse(OrganizationBranch branch) {
        OrganizationBranchResponse response = organizationBranchMapper.toOrganizationBranchResponse(branch);
        employeeRepository.findFirstByBranch_IdAndStatusAndOrgRole_RoleNameOrderByCreatedAtAsc(
                branch.getId(),
                EmployeeStatus.ACTIVE,
                EmployeeConstants.MANAGER_ROLE_NAME
        ).ifPresent(manager -> populateManager(response, manager));
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

    private void validateOrganizationAccess(String organizationId) {
        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {
            return;
        }

        switch (AuthUtils.getDataScope()) {
            case ORGANIZATION -> {
                if (!organizationId.equals(AuthUtils.getOrganizationId())) {
                    throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
                }
            }
            case BRANCH -> {
                OrganizationBranch currentBranch = organizationBranchRepository.findById(AuthUtils.getBranchId())
                        .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
                if (!organizationId.equals(currentBranch.getOrganization().getId())) {
                    throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
                }
            }
            default -> throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    private void validateBranchAccess(OrganizationBranch branch) {
        validateOrganizationAccess(branch.getOrganization().getId());

        if (!AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)
                && AuthUtils.getDataScope() == OrgDataScope.BRANCH
                && !branch.getId().equals(AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private String toKeywordPattern(String keyword) {
        return keyword == null ? null : "%" + keyword.toLowerCase() + "%";
    }

    private Page<OrganizationBranch> findBranches(
            String organizationId,
            String keywordPattern,
            OrganizationBranchStatus status,
            Pageable pageable
    ) {
        if (keywordPattern != null) {
            return organizationBranchRepository.search(
                    organizationId,
                    keywordPattern,
                    status,
                    pageable
            );
        }

        if (organizationId != null && status != null) {
            return organizationBranchRepository.findByOrganizationIdAndStatus(
                    organizationId,
                    status,
                    pageable
            );
        }

        if (organizationId != null) {
            return organizationBranchRepository.findByOrganizationId(
                    organizationId,
                    pageable
            );
        }

        if (status != null) {
            return organizationBranchRepository.findByStatus(
                    status,
                    pageable
            );
        }

        return organizationBranchRepository.findAll(pageable);
    }

    private boolean matchesFilters(
            OrganizationBranch branch,
            String keyword,
            OrganizationBranchStatus status
    ) {
        if (status != null && branch.getStatus() != status) {
            return false;
        }

        if (keyword == null) {
            return true;
        }

        String normalizedKeyword = keyword.toLowerCase();
        return containsIgnoreCase(branch.getBranchName(), normalizedKeyword)
                || containsIgnoreCase(branch.getAddress(), normalizedKeyword)
                || containsIgnoreCase(branch.getPhone(), normalizedKeyword);
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase().contains(normalizedKeyword);
    }
}
