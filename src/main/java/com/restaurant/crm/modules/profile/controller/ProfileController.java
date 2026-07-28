package com.restaurant.crm.modules.profile.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.profile.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.profile.dto.request.ProfileUpdateRequest;
import com.restaurant.crm.modules.profile.dto.request.StaffProfileUpdateRequest;
import jakarta.validation.Valid;
import com.restaurant.crm.modules.profile.service.interfaces.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagingResponse<UserProfileResponse>>> getAllProfiles(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = PaginationConstant.DESC) String direction,
            @RequestParam(required = false, defaultValue = "createdAt") String field
    ) {
        PagingRequest request = PagingRequest.builder()
                .page(page)
                .pageSize(size)
                .sortRequest(SortRequest.builder()
                        .direction(direction)
                        .field(field)
                        .build())
                .build();

        return ResponseEntity.ok(ApiResponse.<PagingResponse<UserProfileResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(profileService.getAll(request))
                .build());
    }

    @GetMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileById(
            @PathVariable String profileId) {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(profileService.getById(profileId))
                .build());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('PROFILE_SELF_UPDATE')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyInfo(
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(profileService.updateMyInfo(request))
                .build());
    }

    @PutMapping("/staff/{employeeId}")
    @PreAuthorize("hasAuthority('PROFILE_UPDATE')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateStaffInfo(
            @PathVariable String employeeId,
            @Valid @RequestBody StaffProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<UserProfileResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(profileService.updateStaffInfo(employeeId, request))
                .build());
    }
}
