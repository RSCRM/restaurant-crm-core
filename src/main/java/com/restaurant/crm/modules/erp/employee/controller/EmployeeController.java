package com.restaurant.crm.modules.erp.employee.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.employee.constants.EmployeeControllerConstants;
import com.restaurant.crm.modules.erp.employee.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.employee.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.employee.service.interfaces.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EmployeeControllerConstants.BASE_PATH)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeController {

    EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> addEmployee(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        EmployeeResponse data = employeeService.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<EmployeeResponse>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(data)
                        .build()
        );
    }
}
