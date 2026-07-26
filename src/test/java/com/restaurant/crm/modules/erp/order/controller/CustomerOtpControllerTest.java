package com.restaurant.crm.modules.erp.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.exception.GlobalExceptionHandler;
import com.restaurant.crm.modules.crm.customer_account.model.OtpRequestResult;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.CustomerOtpService;
import com.restaurant.crm.modules.erp.order.model.TableQrPayload;
import com.restaurant.crm.modules.erp.order.service.interfaces.TableQrTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for uc-c-03 (standalone MockMvc). Asserts the {@code ApiResponse} envelope,
 * error mapping, and — critically — that no response body ever contains a 6-digit OTP code.
 */
class CustomerOtpControllerTest {

    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final String FULL_PHONE = "0987654321";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private TableQrTokenService tableQrTokenService;
    private CustomerOtpService customerOtpService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        tableQrTokenService = mock(TableQrTokenService.class);
        customerOtpService = mock(CustomerOtpService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerOtpController(tableQrTokenService, customerOtpService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void requestReturnsMaskedResponseWithoutOtpCode() throws Exception {
        when(tableQrTokenService.verify("token"))
                .thenReturn(new TableQrPayload("organization-1", BRANCH, TABLE, 1));
        when(customerOtpService.request(anyString(), eq(BRANCH), eq(TABLE)))
                .thenReturn(new OtpRequestResult("0987***321",
                        Instant.parse("2026-07-26T10:15:30Z"), Instant.parse("2026-07-26T10:11:30Z")));

        MvcResult result = mockMvc.perform(post("/api/v1/public/customer/otp/request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("qrToken", "token", "customerPhone", "0987654321"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.maskedPhone").value("0987***321"))
                .andExpect(jsonPath("$.data.attemptsAllowed").value(3))
                .andReturn();

        // Never leak the full phone number — only the masked form.
        assertFalse(result.getResponse().getContentAsString().contains(FULL_PHONE));
    }

    @Test
    void verifyReturnsTicketWithoutOtpCode() throws Exception {
        when(tableQrTokenService.verify("token"))
                .thenReturn(new TableQrPayload("organization-1", BRANCH, TABLE, 1));
        when(customerOtpService.verify(anyString(), eq(BRANCH), eq(TABLE), anyString()))
                .thenReturn("otp-ticket");

        MvcResult result = mockMvc.perform(post("/api/v1/public/customer/otp/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "qrToken", "token", "customerPhone", FULL_PHONE, "otpCode", "654321"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.otpTicket").value("otp-ticket"))
                .andReturn();

        // The submitted OTP code must never be echoed back in the response.
        assertFalse(result.getResponse().getContentAsString().contains("654321"));
    }

    @Test
    void requestRejectsBlankPhoneWithValidationCode() throws Exception {
        mockMvc.perform(post("/api/v1/public/customer/otp/request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("qrToken", "token", "customerPhone", ""))))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode")
                        .value(ErrorCode.CUSTOMER_PHONE_REQUIRED.getCode()));
    }

    @Test
    void requestSurfacesBusinessErrorAsApiResponse() throws Exception {
        when(tableQrTokenService.verify("token"))
                .thenReturn(new TableQrPayload("organization-1", BRANCH, TABLE, 1));
        when(customerOtpService.request(anyString(), any(), any()))
                .thenThrow(new AppException(ErrorCode.OTP_PHONE_LOCKED));

        mockMvc.perform(post("/api/v1/public/customer/otp/request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("qrToken", "token", "customerPhone", "0987654321"))))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode")
                        .value(ErrorCode.OTP_PHONE_LOCKED.getCode()));
    }
}
