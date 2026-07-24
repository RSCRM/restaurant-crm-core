package com.restaurant.crm.modules.erp.invoice.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.invoice.dto.request.CheckoutRequestDto;
import com.restaurant.crm.modules.erp.invoice.dto.response.InvoiceResponse;
import com.restaurant.crm.modules.erp.invoice.service.interfaces.InvoiceService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/erp/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceController {

    InvoiceService invoiceService;

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).PAYMENT_CREATE)")
    public ResponseEntity<ApiResponse<InvoiceResponse>> checkout(
            @Valid @RequestBody CheckoutRequestDto request
    ) {
        InvoiceResponse responseData = invoiceService.checkout(request);
        ApiResponse<InvoiceResponse> response = ApiResponse.<InvoiceResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(responseData)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).PAYMENT_READ)")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceDetails(
            @PathVariable String id
    ) {
        InvoiceResponse responseData = invoiceService.getInvoiceDetails(id);
        ApiResponse<InvoiceResponse> response = ApiResponse.<InvoiceResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(responseData)
                .build();
        return ResponseEntity.ok(response);
    }
}
