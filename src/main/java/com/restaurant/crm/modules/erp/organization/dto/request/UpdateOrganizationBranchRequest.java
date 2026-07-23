package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.organization.constants.OrganizationBranchConstants;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrganizationBranchRequest {

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


    OrganizationBranchStatus status;
}
