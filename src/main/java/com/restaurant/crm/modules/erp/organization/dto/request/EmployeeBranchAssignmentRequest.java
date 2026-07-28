package com.restaurant.crm.modules.erp.organization.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeBranchAssignmentRequest {

    @Schema(
            description = "Employee ID assigned as branch manager",
            example = "f0000000-0000-0000-0000-000000000001"
    )
    @NotBlank(message = "BRANCH_MANAGER_INVALID_REQUEST")
    String managerId;
}
