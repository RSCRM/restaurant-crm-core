package com.restaurant.crm.modules.erp.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.exception.GlobalExceptionHandler;
import com.restaurant.crm.modules.erp.order.dto.request.QrResolveRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionStartRequest;
import com.restaurant.crm.modules.erp.order.dto.response.QrResolveResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.QrSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for uc-c-02 (standalone MockMvc, no Spring context / security):
 * asserts the {@code ApiResponse} envelope and that {@code TQR_*} errors surface as
 * {@code errorMessage.errorCode}. NB: the project's {@code GlobalExceptionHandler}
 * returns HTTP 200 for {@code AppException}, encoding the failure in the body.
 */
class QrSessionControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private QrSessionService qrSessionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        qrSessionService = mock(QrSessionService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new QrSessionController(qrSessionService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void resolveReturnsApiResponseWithData() throws Exception {
        when(qrSessionService.resolve(any(QrResolveRequest.class))).thenReturn(
                QrResolveResponse.builder()
                        .organizationId("organization-1")
                        .branchId("branch-1")
                        .branchName("Branch One")
                        .areaName("Main")
                        .tableNumber("A1")
                        .capacity(4)
                        .tableStatus("AVAILABLE")
                        .joinable(true)
                        .hasActiveSession(false)
                        .build());

        mockMvc.perform(post("/api/v1/public/customer/qr/resolve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("qrToken", "token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.branchId").value("branch-1"))
                .andExpect(jsonPath("$.data.tableNumber").value("A1"))
                .andExpect(jsonPath("$.data.hasActiveSession").value(false));
    }

    @Test
    void resolveRejectsBlankTokenWithValidationErrorCode() throws Exception {
        mockMvc.perform(post("/api/v1/public/customer/qr/resolve")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("qrToken", ""))))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode")
                        .value(ErrorCode.TQR_TOKEN_INVALID.getCode()));
    }

    @Test
    void startSurfacesBusinessErrorAsApiResponse() throws Exception {
        when(qrSessionService.start(any(QrSessionStartRequest.class)))
                .thenThrow(new AppException(ErrorCode.TQR_TABLE_SESSION_EXISTS));

        mockMvc.perform(post("/api/v1/public/customer/qr/session")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "qrToken", "token",
                                "customerPhone", "0900000000",
                                "otpTicket", "ticket"))))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode")
                        .value(ErrorCode.TQR_TABLE_SESSION_EXISTS.getCode()));
    }
}
