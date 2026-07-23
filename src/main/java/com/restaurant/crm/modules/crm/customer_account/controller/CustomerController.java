package com.restaurant.crm.modules.crm.customer_account.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.customer_account.dto.request.CustomerIdentifyRequest;
import com.restaurant.crm.modules.crm.customer_account.dto.response.CustomerResponse;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.CustomerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crm/customers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {

    CustomerService customerService;

    @PostMapping("/identify")
    public ResponseEntity<ApiResponse<CustomerResponse>> identifyCustomer(@RequestBody @Valid CustomerIdentifyRequest request) {
        CustomerResponse response = customerService.identifyAndInitializeWallet(request);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable String id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.<CustomerResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagingResponse<CustomerResponse>>> getAllCustomers(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<CustomerResponse> response = customerService.getAllCustomers(page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<CustomerResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }
}
