package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.crm.customer_account.constants.CustomerOtpConstants;
import com.restaurant.crm.modules.crm.customer_account.model.OtpRequestResult;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.CustomerOtpService;
import com.restaurant.crm.modules.erp.order.constants.CustomerOtpControllerConstants;
import com.restaurant.crm.modules.erp.order.dto.request.OtpRequestRequest;
import com.restaurant.crm.modules.erp.order.dto.request.OtpVerifyRequest;
import com.restaurant.crm.modules.erp.order.dto.response.OtpRequestResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OtpVerifyResponse;
import com.restaurant.crm.modules.erp.order.model.TableQrPayload;
import com.restaurant.crm.modules.erp.order.service.interfaces.TableQrTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;


@Tag(name = "Customer OTP", description = "identify a customer by phone + OTP")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerOtpController {

    TableQrTokenService tableQrTokenService;
    CustomerOtpService customerOtpService;

    @Operation(summary = "Request an OTP for the scanned table (public)")
    @PostMapping(CustomerOtpControllerConstants.BASE_PUBLIC + CustomerOtpControllerConstants.PATH_REQUEST)
    public ResponseEntity<ApiResponse<OtpRequestResponse>> request(
            @Valid @RequestBody OtpRequestRequest request) {
        TableQrPayload payload = tableQrTokenService.verify(request.getQrToken());
        OtpRequestResult result = customerOtpService.request(
                request.getCustomerPhone(), payload.branchId(), payload.tableId());

        return ResponseEntity.ok(ApiResponse.<OtpRequestResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(OtpRequestResponse.builder()
                        .maskedPhone(result.maskedPhone())
                        .expiresAt(result.expiresAt())
                        .resendAvailableAt(result.resendAvailableAt())
                        .attemptsAllowed(CustomerOtpConstants.MAX_ATTEMPTS)
                        .build())
                .build());
    }

    @Operation(summary = "Verify the entered OTP and get an otpTicket (public)")
    @PostMapping(CustomerOtpControllerConstants.BASE_PUBLIC + CustomerOtpControllerConstants.PATH_VERIFY)
    public ResponseEntity<ApiResponse<OtpVerifyResponse>> verify(
            @Valid @RequestBody OtpVerifyRequest request) {
        TableQrPayload payload = tableQrTokenService.verify(request.getQrToken());
        String otpTicket = customerOtpService.verify(
                request.getCustomerPhone(), payload.branchId(), payload.tableId(), request.getOtpCode());

        return ResponseEntity.ok(ApiResponse.<OtpVerifyResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(OtpVerifyResponse.builder()
                        .otpTicket(otpTicket)
                        .ticketExpiresAt(Instant.now().plusSeconds(CustomerOtpConstants.TICKET_TTL_SECONDS))
                        .build())
                .build());
    }
}
