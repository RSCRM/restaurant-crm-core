package com.restaurant.crm.modules.profile.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.profile.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.profile.dto.request.ProfileUpdateRequest;
import com.restaurant.crm.modules.profile.dto.request.StaffProfileUpdateRequest;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.profile.constants.permission.ProfilePermissionConstants;
import jakarta.validation.Valid;
import com.restaurant.crm.modules.profile.service.interfaces.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileController {

    ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyInfo() {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(profileService.getMyInfo())
                .build());
    }

    @PutMapping("/me")
    @org.springframework.security.access.prepost.PreAuthorize(
            "hasAuthority(T(com.restaurant.crm.modules.profile.constants.permission.ProfilePermissionConstants).SELF_UPDATE)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyInfo(
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(profileService.updateMyInfo(request))
                .build());
    }

    @PutMapping("/staff/{employeeId}")
    @org.springframework.security.access.prepost.PreAuthorize(
            "@employeeAccessChecker.canManage('" + EmployeeConstants.EMPLOYEE_UPDATE + "')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateStaffInfo(
            @PathVariable String employeeId,
            @Valid @RequestBody StaffProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(profileService.updateStaffInfo(employeeId, request))
                .build());
    }
}
