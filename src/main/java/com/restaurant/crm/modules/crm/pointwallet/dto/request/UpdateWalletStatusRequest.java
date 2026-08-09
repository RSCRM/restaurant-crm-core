package com.restaurant.crm.modules.crm.pointwallet.dto.request;

import com.restaurant.crm.modules.crm.customeraccount.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.pointwallet.constants.CustomerPointConstants;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWalletStatusRequest {

    @NotNull(message = CustomerPointConstants.CUSTOMER_STATUS_REQUIRE)
    CustomerStatus status;

    @NotNull(message =CustomerPointConstants.ORGANIZATION_ID_REQUIRED)
    String organizationId;
}
