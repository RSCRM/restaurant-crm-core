package com.restaurant.crm.modules.erp.menu.mapper;

import com.restaurant.crm.modules.erp.menu.constants.CustomerMenuConstants;
import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuComboResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuModifierGroupResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuModifierOptionResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuProductResponse;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierGroup;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * Maps menu entities to customer-facing DTOs (uc-c-04).
 * {@code available} is derived from the String {@code status}; {@code branchId} is never mapped out.
 */
@Mapper(componentModel = "spring")
public interface CustomerMenuMapper {

    @Mapping(target = "productId", source = "id")
    @Mapping(target = "available", source = "status", qualifiedByName = "statusToAvailable")
    MenuProductResponse toProductResponse(Product product);

    List<MenuProductResponse> toProductResponses(List<Product> products);

    @Mapping(target = "comboId", source = "id")
    @Mapping(target = "available", source = "status", qualifiedByName = "statusToAvailable")
    @Mapping(target = "items", ignore = true) // no Combo↔Product join yet (TODO uc-c-04)
    MenuComboResponse toComboResponse(Combo combo);

    List<MenuComboResponse> toComboResponses(List<Combo> combos);

    @Mapping(target = "modifierOptionId", source = "id")
    @Mapping(target = "available", source = "status", qualifiedByName = "statusToAvailable")
    MenuModifierOptionResponse toModifierOptionResponse(ModifierOption option);

    List<MenuModifierOptionResponse> toModifierOptionResponses(List<ModifierOption> options);

    @Mapping(target = "modifierGroupId", source = "group.id")
    @Mapping(target = "options", source = "options")
    MenuModifierGroupResponse toModifierGroupResponse(ModifierGroup group, List<MenuModifierOptionResponse> options);

    @Named("statusToAvailable")
    default boolean statusToAvailable(String status) {
        return CustomerMenuConstants.STATUS_AVAILABLE.equals(status);
    }
}
