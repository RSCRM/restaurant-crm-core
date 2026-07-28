package com.restaurant.crm.modules.erp.menu.mapper;

import com.restaurant.crm.modules.erp.menu.entity.Combo;
import com.restaurant.crm.modules.erp.menu.entity.Category;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuComboResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuModifierGroupResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuModifierOptionResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuProductResponse;
import com.restaurant.crm.modules.erp.menu.entity.ModifierGroup;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerMenuMapperTest {

    private final CustomerMenuMapper mapper = Mappers.getMapper(CustomerMenuMapper.class);

    @Test
    void toProductResponseMapsIdPriceAndAvailability() {
        Product product = Product.builder()
                .id("product-1")
                .branch(OrganizationBranch.builder().id("branch-1").build())
                .category(Category.builder().id("cat-1").build())
                .productName("Phở bò")
                .description("Beef noodle")
                .price(new BigDecimal("12.34"))
                .imageUrl("http://img/1.png")
                .status("AVAILABLE")
                .requiresPreparation(true)
                .build();

        MenuProductResponse response = mapper.toProductResponse(product);

        assertEquals("product-1", response.getProductId());
        assertEquals("Phở bò", response.getProductName());
        assertEquals(new BigDecimal("12.34"), response.getPrice()); // exact 2 decimals, no rounding
        assertTrue(response.isAvailable());
        assertTrue(response.getRequiresPreparation());
    }

    @Test
    void toProductResponseMarksNonAvailableStatusUnavailable() {
        Product product = Product.builder()
                .id("product-2").branch(OrganizationBranch.builder().id("branch-1").build())
                .category(Category.builder().id("cat-1").build())
                .productName("Sold out dish").price(new BigDecimal("9.00"))
                .status("OUT_OF_STOCK").requiresPreparation(false).build();

        assertFalse(mapper.toProductResponse(product).isAvailable());
    }

    @Test
    void toComboResponseHasEmptyItems() {
        Combo combo = Combo.builder()
                .id("combo-1").branch(OrganizationBranch.builder().id("branch-1").build()).comboName("Family set")
                .price(new BigDecimal("50.00")).status("AVAILABLE").build();

        MenuComboResponse response = mapper.toComboResponse(combo);

        assertEquals("combo-1", response.getComboId());
        assertTrue(response.isAvailable());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void toModifierGroupResponseAttachesOptions() {
        ModifierGroup group = ModifierGroup.builder()
                .id("group-1").groupName("Toppings")
                .minSelection(0).maxSelection(3).build();
        ModifierOption option = ModifierOption.builder()
                .id("option-1").optionName("Extra cheese")
                .additionalPrice(new BigDecimal("1.50")).status("AVAILABLE").build();

        MenuModifierGroupResponse response = mapper.toModifierGroupResponse(
                group, List.of(mapper.toModifierOptionResponse(option)));

        assertEquals("group-1", response.getModifierGroupId());
        assertEquals("Toppings", response.getGroupName());
        assertEquals(0, response.getMinSelection());
        assertEquals(3, response.getMaxSelection());
        assertEquals(1, response.getOptions().size());
        MenuModifierOptionResponse opt = response.getOptions().get(0);
        assertEquals("option-1", opt.getModifierOptionId());
        assertEquals(new BigDecimal("1.50"), opt.getAdditionalPrice());
        assertTrue(opt.isAvailable());
    }
}
