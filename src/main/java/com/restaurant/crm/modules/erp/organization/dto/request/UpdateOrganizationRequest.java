package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.shared.constants.organization.OrganizationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrganizationRequest {


    @Size(max = OrganizationConstants.MAX_CHARS_ORGANIZATION_NAME)
    String organizationName;


    @Size(max = OrganizationConstants.MAX_CHARS_TAX_CODE)
    String taxCode;


    @Size(max = OrganizationConstants.MAX_CHARS_ADDRESS)
    String address;


    @Size(max = OrganizationConstants.MAX_CHARS_PHONE)
    String phone;


    @Email
    @Size(max = OrganizationConstants.MAX_CHARS_EMAIL)
    String email;
}
