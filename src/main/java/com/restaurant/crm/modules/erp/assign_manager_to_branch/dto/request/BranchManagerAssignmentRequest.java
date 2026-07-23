package com.restaurant.crm.modules.erp.assign_manager_to_branch.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BranchManagerAssignmentRequest {

    @NotBlank(message = "BRANCH_MANAGER_INVALID_REQUEST")
    String branchManagerId;
}
