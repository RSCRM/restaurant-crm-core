package com.restaurant.crm.modules.erp.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.exception.GlobalExceptionHandler;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartAddItemRequest;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartSubmitResponse;
import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartService;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartSseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for uc-c-05 (standalone MockMvc). Asserts the {@code ApiResponse} envelope, error
 * mapping (HTTP 200 + {@code success=false} per this repo's handler), and that cart items never
 * expose {@code branchId}.
 */
class GroupCartControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private GroupCartService groupCartService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        groupCartService = mock(GroupCartService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new GroupCartController(groupCartService, mock(GroupCartSseService.class)))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getCartReturnsApiResponseAndHidesBranchIdAtItemLevel() throws Exception {
        GroupCartItemResponse item = GroupCartItemResponse.builder()
                .cartItemId("ci1").productId("p1").name("Phở").unitPrice(new BigDecimal("10.00"))
                .quantity(2).lineTotal(new BigDecimal("20.00")).build();
        GroupCartResponse cart = GroupCartResponse.builder()
                .sessionId("session-1").tableId("table-1").role(SessionMemberRole.OWNER)
                .items(List.of(item)).subtotal(new BigDecimal("20.00")).itemCount(1).submitAllowed(true).build();
        when(groupCartService.getCart()).thenReturn(cart);

        mockMvc.perform(get("/api/v1/customer/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value("session-1"))
                .andExpect(jsonPath("$.data.items[0].cartItemId").value("ci1"))
                .andExpect(jsonPath("$.data.items[0].branchId").doesNotExist());
    }

    @Test
    void submitReturnsOrderId() throws Exception {
        when(groupCartService.submit()).thenReturn(GroupCartSubmitResponse.builder()
                .orderId("order-1").itemCount(1).subtotal(new BigDecimal("20.00")).build());

        mockMvc.perform(post("/api/v1/customer/cart/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("order-1"));
    }

    @Test
    void submitSurfacesEmptyCartAsApiResponse() throws Exception {
        when(groupCartService.submit()).thenThrow(new AppException(ErrorCode.CART_EMPTY));

        mockMvc.perform(post("/api/v1/customer/cart/submit"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode").value(ErrorCode.CART_EMPTY.getCode()));
    }

    @Test
    void addItemRejectsInvalidSelectionViaValidation() throws Exception {
        // Neither productId nor comboId → @AssertTrue XOR fails → CART_ITEM_REQUEST_INVALID.
        mockMvc.perform(post("/api/v1/customer/cart/items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("quantity", 1))))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode")
                        .value(ErrorCode.CART_ITEM_REQUEST_INVALID.getCode()));
    }

    @Test
    void addItemDelegatesToService() throws Exception {
        when(groupCartService.addItem(any(GroupCartAddItemRequest.class))).thenReturn(
                GroupCartResponse.builder().sessionId("session-1").itemCount(1)
                        .subtotal(new BigDecimal("10.00")).build());

        mockMvc.perform(post("/api/v1/customer/cart/items")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("productId", "p1", "quantity", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemCount").value(1));
    }
}
