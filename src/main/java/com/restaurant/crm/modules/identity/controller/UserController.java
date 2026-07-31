package com.restaurant.crm.modules.identity.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.identity.dto.request.UserCreationRequest;
import com.restaurant.crm.modules.identity.dto.request.UserRolesUpdateRequest;
import com.restaurant.crm.modules.identity.dto.response.UserResponse;
import com.restaurant.crm.modules.identity.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreationRequest request
    ) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(userService.create(request))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagingResponse<UserResponse>>> getUsers(
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

        ApiResponse<PagingResponse<UserResponse>> response = ApiResponse.<PagingResponse<UserResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(userService.getUsers(request))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(userService.getById(userId))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable String userId,
            @Valid @RequestBody UserRolesUpdateRequest request
    ) {
        ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(userService.updateRoles(userId, request))
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        userService.softDeleteById(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(ApiConstant.SUCCESS)
                .build();

        return ResponseEntity.ok(response);
    }
}
