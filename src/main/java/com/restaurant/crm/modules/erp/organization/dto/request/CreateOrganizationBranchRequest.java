package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.organization.constants.OrganizationBranchConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrganizationBranchRequest {

    String organizationId;


    @NotBlank
    @Size(
            max = OrganizationBranchConstants.MAX_CHARS_BRANCH_NAME
    )
    String branchName;


    @Size(
            max = OrganizationBranchConstants.MAX_CHARS_ADDRESS
    )
    String address;


    @Size(
            max = OrganizationBranchConstants.MAX_CHARS_PHONE
    )
    String phone;
}
