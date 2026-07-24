package com.restaurant.crm.modules.erp.table.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.table.dto.request.TableSessionCreationRequest;
import com.restaurant.crm.modules.erp.table.dto.request.TableSessionTransferRequest;
import com.restaurant.crm.modules.erp.table.dto.response.TableSessionResponse;
import com.restaurant.crm.modules.erp.table.service.interfaces.TableSessionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/table-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableSessionController {

    TableSessionService tableSessionService;

    @PostMapping
    @PreAuthorize("hasAuthority('TABLE_MANAGE') or hasAuthority('ORDER_CREATE')")
    public ResponseEntity<ApiResponse<TableSessionResponse>> create(
            @Valid @RequestBody TableSessionCreationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TableSessionResponse>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(tableSessionService.create(request))
                        .build());
    }

    @PutMapping("/{sessionId}/transfer")
    @PreAuthorize("hasAuthority('TABLE_MANAGE') or hasAuthority('ORDER_UPDATE')")
    public ResponseEntity<ApiResponse<TableSessionResponse>> transfer(
            @PathVariable String sessionId,
            @Valid @RequestBody TableSessionTransferRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<TableSessionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(tableSessionService.transfer(sessionId, request))
                .build());
    }
}
