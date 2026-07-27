package com.restaurant.crm.modules.erp.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.exception.GlobalExceptionHandler;
import com.restaurant.crm.modules.erp.menu.dto.response.CustomerMenuResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuCategoryResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuProductResponse;
import com.restaurant.crm.modules.erp.menu.service.interfaces.CustomerMenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for uc-c-04 (standalone MockMvc). Asserts the {@code ApiResponse} envelope, error
 * mapping (HTTP 200 with {@code success=false} per this repo's handler), and that product objects
 * never expose {@code branchId}.
 */
class CustomerMenuControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CustomerMenuService customerMenuService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        customerMenuService = mock(CustomerMenuService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CustomerMenuController(customerMenuService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void getMenuReturnsApiResponseAndHidesBranchIdAtProductLevel() throws Exception {
        MenuProductResponse product = MenuProductResponse.builder()
                .productId("p1").productName("Apple").price(new BigDecimal("10.00"))
                .available(true).requiresPreparation(true).build();
        CustomerMenuResponse menu = CustomerMenuResponse.builder()
                .branchId("branch-1")
                .categories(List.of(MenuCategoryResponse.builder()
                        .categoryId("cat-1").categoryName(null).products(List.of(product)).build()))
                .build();
        when(customerMenuService.getMenu()).thenReturn(menu);

        mockMvc.perform(get("/api/v1/customer/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.branchId").value("branch-1"))
                .andExpect(jsonPath("$.data.categories[0].products[0].productName").value("Apple"))
                .andExpect(jsonPath("$.data.categories[0].categoryName").doesNotExist())
                .andExpect(jsonPath("$.data.categories[0].products[0].branchId").doesNotExist());
    }

    @Test
    void getMenuSurfacesMenuEmptyAsApiResponse() throws Exception {
        when(customerMenuService.getMenu()).thenThrow(new AppException(ErrorCode.MENU_EMPTY));

        mockMvc.perform(get("/api/v1/customer/menu"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode").value(ErrorCode.MENU_EMPTY.getCode()));
    }

    @Test
    void getMenuSurfacesBranchContextMissingAsApiResponse() throws Exception {
        when(customerMenuService.getMenu())
                .thenThrow(new AppException(ErrorCode.MENU_BRANCH_CONTEXT_MISSING));

        mockMvc.perform(get("/api/v1/customer/menu"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode")
                        .value(ErrorCode.MENU_BRANCH_CONTEXT_MISSING.getCode()));
    }

    @Test
    void getProductReturnsProduct() throws Exception {
        when(customerMenuService.getProduct("p1")).thenReturn(MenuProductResponse.builder()
                .productId("p1").productName("Apple").price(new BigDecimal("10.00")).available(true).build());

        mockMvc.perform(get("/api/v1/customer/menu/products/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value("p1"))
                .andExpect(jsonPath("$.data.branchId").doesNotExist());
    }

    @Test
    void getProductSurfacesNotFoundAsApiResponse() throws Exception {
        when(customerMenuService.getProduct("p9"))
                .thenThrow(new AppException(ErrorCode.MENU_PRODUCT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/customer/menu/products/p9"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage.errorCode")
                        .value(ErrorCode.MENU_PRODUCT_NOT_FOUND.getCode()));
    }
}
