package com.restaurant.crm.modules.erp.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.dto.request.BranchManagerAssignmentRequest;
import com.restaurant.crm.modules.erp.dto.response.BranchManagerResponse;
import com.restaurant.crm.modules.erp.service.interfaces.BranchManagerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personal/branches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchManagerAssignmentController {

    BranchManagerService branchManagerService;

    @PutMapping("/{branchId}/manager")
    public ResponseEntity<ApiResponse<BranchManagerResponse>> assignManagerToBranch(
            @PathVariable String branchId,
            @Valid @RequestBody BranchManagerAssignmentRequest request
    ) {
        ApiResponse<BranchManagerResponse> response = ApiResponse.<BranchManagerResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(branchManagerService.assignToBranch(branchId, request))
                .build();

        return ResponseEntity.ok(response);
    }
}
