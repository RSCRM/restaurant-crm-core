package com.restaurant.crm.modules.erp.organization.mapper;

import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationBranchResponse;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrganizationBranchMapper {


    @Mapping(target = "id", ignore = true)

    // Set manually in service
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "manager", ignore = true)

    @Mapping(target = "status", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    OrganizationBranch toOrganizationBranch(
            CreateOrganizationBranchRequest request
    );


    @Mapping(
            source = "organization.id",
            target = "organizationId"
    )
    @Mapping(target = "managerId", source = "manager.id")
    OrganizationBranchResponse toOrganizationBranchResponse(
            OrganizationBranch branch
    );


    @Mapping(target = "id", ignore = true)

    // Prevent changing parent organization
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "manager", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)

    void updateOrganizationBranch(
            UpdateOrganizationBranchRequest request,
            @MappingTarget OrganizationBranch branch
    );
}
