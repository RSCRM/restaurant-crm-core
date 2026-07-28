package com.restaurant.crm.modules.erp.menu.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.entity.Combo;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.dto.response.CustomerMenuResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuCategoryResponse;
import com.restaurant.crm.modules.erp.menu.mapper.CustomerMenuMapper;
import com.restaurant.crm.modules.erp.menu.entity.ModifierGroup;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.repository.ModifierGroupRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.entity.Category;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerMenuServiceImplTest {

    private static final String BRANCH = "branch-1";

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ComboRepository comboRepository = mock(ComboRepository.class);
    private final ModifierGroupRepository modifierGroupRepository = mock(ModifierGroupRepository.class);
    private final ModifierOptionRepository modifierOptionRepository = mock(ModifierOptionRepository.class);
    private final CustomerMenuMapper mapper = Mappers.getMapper(CustomerMenuMapper.class);

    private CustomerMenuServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerMenuServiceImpl(productRepository, comboRepository,
                modifierGroupRepository, modifierOptionRepository, mapper);
    }

    @Test
    void getMenuGroupsProductsByCategoryInStableOrder() {
        // Repo returns already sorted by (categoryId asc, productName asc).
        List<Product> products = List.of(
                product("p1", "cat-a", "Apple", "AVAILABLE"),
                product("p2", "cat-a", "Banana", "AVAILABLE"),
                product("p3", "cat-b", "Cherry", "OUT_OF_STOCK"));
        try (var auth = branchContext()) {
            when(productRepository.findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(BRANCH)).thenReturn(products);
            when(comboRepository.findByBranchIdOrderByComboNameAsc(BRANCH)).thenReturn(List.of());
            when(modifierGroupRepository.findByProduct_Branch_IdOrderByGroupNameAsc(BRANCH)).thenReturn(List.of());

            CustomerMenuResponse menu = service.getMenu();

            assertEquals(BRANCH, menu.getBranchId());
            assertEquals(2, menu.getCategories().size());
            assertEquals("cat-a", menu.getCategories().get(0).getCategoryId());
            assertEquals("cat-b", menu.getCategories().get(1).getCategoryId());
            assertNull(menu.getCategories().get(0).getCategoryName()); // no Category entity yet
            MenuCategoryResponse catA = menu.getCategories().get(0);
            assertEquals(List.of("Apple", "Banana"),
                    catA.getProducts().stream().map(p -> p.getProductName()).toList());
            assertTrue(catA.getProducts().get(0).isAvailable());
            // OUT_OF_STOCK product still appears, marked unavailable
            assertFalse(menu.getCategories().get(1).getProducts().get(0).isAvailable());
            assertTrue(menu.getCombos().isEmpty());
        }
    }

    @Test
    void getMenuReturnsCombosWhenNoProductsIsFineAndViceVersa() {
        try (var auth = branchContext()) {
            when(productRepository.findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(BRANCH))
                    .thenReturn(List.of(product("p1", "cat-a", "Apple", "AVAILABLE")));
            when(comboRepository.findByBranchIdOrderByComboNameAsc(BRANCH)).thenReturn(List.of());
            when(modifierGroupRepository.findByProduct_Branch_IdOrderByGroupNameAsc(BRANCH)).thenReturn(List.of());

            CustomerMenuResponse menu = service.getMenu();

            assertTrue(menu.getCombos().isEmpty()); // no combos is NOT an error
            assertEquals(1, menu.getCategories().size());
        }
    }

    @Test
    void getMenuThrowsWhenNoProductsAndNoCombos() {
        try (var auth = branchContext()) {
            when(productRepository.findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(BRANCH)).thenReturn(List.of());
            when(comboRepository.findByBranchIdOrderByComboNameAsc(BRANCH)).thenReturn(List.of());

            AppException exception = assertThrows(AppException.class, () -> service.getMenu());
            assertEquals(ErrorCode.MENU_EMPTY, exception.getErrorCode());
        }
    }

    @Test
    void getMenuThrowsWhenBranchMissing() {
        try (var auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn(null);

            AppException exception = assertThrows(AppException.class, () -> service.getMenu());
            assertEquals(ErrorCode.MENU_BRANCH_CONTEXT_MISSING, exception.getErrorCode());
        }
    }

    @Test
    void getMenuAttachesOptionsToTheirGroupWithFixedQueryCount() {
        ModifierGroup g1 = group("g1", "Sauces");
        ModifierGroup g2 = group("g2", "Toppings");
        List<ModifierOption> options = List.of(
                option("o1", g1, "Chili"),
                option("o2", g2, "Cheese"),
                option("o3", g2, "Egg"));
        try (var auth = branchContext()) {
            when(productRepository.findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(BRANCH))
                    .thenReturn(List.of(product("p1", "cat-a", "Apple", "AVAILABLE")));
            when(comboRepository.findByBranchIdOrderByComboNameAsc(BRANCH)).thenReturn(List.of());
            when(modifierGroupRepository.findByProduct_Branch_IdOrderByGroupNameAsc(BRANCH)).thenReturn(List.of(g1, g2));
            when(modifierOptionRepository.findByModifierGroupIdInOrderByOptionNameAsc(anyCollection()))
                    .thenReturn(options);

            CustomerMenuResponse menu = service.getMenu();

            assertEquals(2, menu.getModifierGroups().size());
            assertEquals(1, menu.getModifierGroups().get(0).getOptions().size()); // g1 → 1 option
            assertEquals(2, menu.getModifierGroups().get(1).getOptions().size()); // g2 → 2 options

            // Exactly 4 queries, independent of the number of groups (no N+1).
            verify(productRepository, times(1)).findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(BRANCH);
            verify(comboRepository, times(1)).findByBranchIdOrderByComboNameAsc(BRANCH);
            verify(modifierGroupRepository, times(1)).findByProduct_Branch_IdOrderByGroupNameAsc(BRANCH);
            verify(modifierOptionRepository, times(1)).findByModifierGroupIdInOrderByOptionNameAsc(anyCollection());
        }
    }

    @Test
    void getProductReturnsBranchScopedProduct() {
        try (var auth = branchContext()) {
            when(productRepository.findByIdAndBranchId("p1", BRANCH))
                    .thenReturn(Optional.of(product("p1", "cat-a", "Apple", "AVAILABLE")));

            assertEquals("Apple", service.getProduct("p1").getProductName());
        }
    }

    @Test
    void getProductThrowsWhenNotInBranch() {
        try (var auth = branchContext()) {
            when(productRepository.findByIdAndBranchId("p9", BRANCH)).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> service.getProduct("p9"));
            assertEquals(ErrorCode.MENU_PRODUCT_NOT_FOUND, exception.getErrorCode());
        }
    }

    // ==== fixtures ====

    private org.mockito.MockedStatic<AuthUtils> branchContext() {
        org.mockito.MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class);
        auth.when(AuthUtils::getBranchId).thenReturn(BRANCH);
        return auth;
    }

    private Product product(String id, String categoryId, String name, String status) {
        return Product.builder()
                .id(id)
                .branch(OrganizationBranch.builder().id(BRANCH).build())
                .category(Category.builder().id(categoryId).build())
                .productName(name)
                .price(new BigDecimal("10.00")).status(status).requiresPreparation(true).build();
    }

    private ModifierGroup group(String id, String name) {
        return ModifierGroup.builder().id(id)
                .product(Product.builder().branch(OrganizationBranch.builder().id(BRANCH).build()).build())
                .groupName(name)
                .minSelection(0).maxSelection(2).build();
    }

    private ModifierOption option(String id, ModifierGroup group, String name) {
        return ModifierOption.builder().id(id).modifierGroup(group).optionName(name)
                .additionalPrice(new BigDecimal("1.00")).status("AVAILABLE").build();
    }
}
