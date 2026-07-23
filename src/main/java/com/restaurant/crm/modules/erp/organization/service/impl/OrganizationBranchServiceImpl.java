package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationBranchResponse;
import com.restaurant.crm.modules.erp.shared.entity.Organization;
import com.restaurant.crm.modules.erp.shared.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.mapper.OrganizationBranchMapper;
import com.restaurant.crm.modules.erp.shared.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.shared.repository.OrganizationRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationBranchService;
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
public class OrganizationBranchServiceImpl implements OrganizationBranchService {

    OrganizationBranchRepository organizationBranchRepository;
    OrganizationRepository organizationRepository;
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

        return organizationBranchMapper.toOrganizationBranchResponse(savedOrganizationBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationBranchResponse getOrganizationBranchById(String id) {

        OrganizationBranch organizationBranch =
                organizationBranchRepository.findById(id)
                        .orElseThrow(() ->
                                new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        return organizationBranchMapper.toOrganizationBranchResponse(organizationBranch);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<OrganizationBranchResponse> getOrganizationBranches(
            String organizationId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
        );

        Page<OrganizationBranch> organizationBranchPage =
                organizationBranchRepository.findByOrganizationId(
                        organizationId,
                        pageable
                );

        return PagingResponse.<OrganizationBranchResponse>builder()
                .currentPage(page)
                .pageSize(organizationBranchPage.getSize())
                .totalPages(organizationBranchPage.getTotalPages())
                .totalElement(organizationBranchPage.getTotalElements())
                .data(
                        organizationBranchPage.getContent()
                                .stream()
                                .map(organizationBranchMapper::toOrganizationBranchResponse)
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

        return organizationBranchMapper.toOrganizationBranchResponse(updatedOrganizationBranch);
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
}