package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.order.dto.response.KdsActiveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.KdsItemResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.KdsService;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kds")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KdsController {

    KdsService kdsService;
    SseEmitterService sseEmitterService;
    EmployeeRepository employeeRepository;

    /**
     * Retrieves the KDS items list.
     * Accessible by employees with ORDER_READ permission (Chefs).
     *
     * @param section the section to query (ACTIVE or HISTORY)
     * @return the ApiResponse containing active groups/items or history items
     */
    @GetMapping("/items")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).ORDER_READ)")
    public ResponseEntity<ApiResponse<Object>> getKdsItems(
            @RequestParam(value = "section", defaultValue = "ACTIVE") String section
    ) {
        Object data;
        if ("HISTORY".equalsIgnoreCase(section)) {
            List<KdsItemResponse> history = kdsService.getHistoryItems();
            data = history;
        } else {
            KdsActiveResponse active = kdsService.getActiveItems();
            data = active;
        }

        ApiResponse<Object> response = ApiResponse.builder()
                .success(ApiConstant.SUCCESS)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Subscribes the KDS screen to real-time events for the chef's branch.
     * Accessible by employees with ORDER_READ permission.
     *
     * @return the SseEmitter connection
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).ORDER_READ)")
    public SseEmitter subscribe() {
        String employeeId = AuthUtils.getEmployeeId();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        if (employee.getBranch() == null) {
            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND);
        }
        return sseEmitterService.createEmitter(employee.getBranch().getId());
    }
}
