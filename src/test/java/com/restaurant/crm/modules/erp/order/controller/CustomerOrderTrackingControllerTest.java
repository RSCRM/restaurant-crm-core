package com.restaurant.crm.modules.erp.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.exception.GlobalExceptionHandler;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingSummaryResponse;
import com.restaurant.crm.modules.erp.order.enums.CustomerOrderStage;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerOrderTrackingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for uc-c-06 (standalone MockMvc). Asserts the {@code ApiResponse} envelope, error
 * mapping, and — the key AC — that the response never leaks {@code customerPhone} or a phone number.
 * NB: {@code @PreAuthorize} is NOT evaluated in this slice, so this does not prove authorization.
 */
class CustomerOrderTrackingControllerTest {

    private static final String OWNER_PHONE = "0987654321";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private CustomerOrderTrackingService trackingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        trackingService = mock(CustomerOrderTrackingService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerOrderTrackingController(trackingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void cookingStatusReturnsApiResponseWithoutPhone() throws Exception {
        CustomerOrderTrackingResponse response = CustomerOrderTrackingResponse.builder()
                .hasActiveOrder(true)
                .orderId("order-1").orderCode("OC-1").tableId("table-1")
                .orderStatus(OrderStatus.PENDING)
                .subtotal(new BigDecimal("250000.00"))
                .discountAmount(new BigDecimal("0.00"))
                .totalAmount(new BigDecimal("250000.00"))
                .summary(CustomerOrderTrackingSummaryResponse.builder()
                        .total(1).received(0).cooking(1).readyToServe(0).served(0).cancelled(0).build())
                .items(List.of(CustomerOrderTrackingItemResponse.builder()
                        .orderItemId("i1").itemName("Phở").quantity(2).note("không cay")
                        .status(OrderItemStatus.IN_PROGRESS)
                        .customerStage(CustomerOrderStage.COOKING).stageOrder(2).build()))
                .build();
        when(trackingService.getCurrentCookingStatus()).thenReturn(response);

        MvcResult result = mockMvc.perform(get("/api/v1/customer/orders/current/cooking-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasActiveOrder").value(true))
                .andExpect(jsonPath("$.data.items[0].customerStage").value("COOKING"))
                .andExpect(jsonPath("$.data.items[0].stageOrder").value(2))
                .andExpect(jsonPath("$.data.customerPhone").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].cancelReason").doesNotExist())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("customerPhone"), "must not expose the customerPhone key");
        assertFalse(body.contains(OWNER_PHONE), "must not expose any phone number");
    }

    @Test
    void cookingStatusSurfacesBusinessErrorAsApiResponse() throws Exception {
        when(trackingService.getCurrentCookingStatus())
                .thenThrow(new AppException(ErrorCode.TQR_SESSION_NOT_FOUND));

        mockMvc.perform(get("/api/v1/customer/orders/current/cooking-status"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode").value(ErrorCode.TQR_SESSION_NOT_FOUND.getCode()));
    }

    @Test
    void subscribeSurfacesNoActiveOrderAsApiResponse() throws Exception {
        when(trackingService.subscribeToCookingStatus())
                .thenThrow(new AppException(ErrorCode.TRACK_NO_ACTIVE_ORDER));

        mockMvc.perform(get("/api/v1/customer/orders/current/cooking-status/subscribe"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode").value(ErrorCode.TRACK_NO_ACTIVE_ORDER.getCode()));
    }

    @Test
    void subscribeStartsSseStreamWhenOrderExists() throws Exception {
        when(trackingService.subscribeToCookingStatus())
                .thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/api/v1/customer/orders/current/cooking-status/subscribe"))
                .andExpect(request().asyncStarted());
    }
}
