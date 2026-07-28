package com.restaurant.crm.modules.erp.menu.mapper;

import com.restaurant.crm.modules.erp.menu.dto.response.CategoryResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboItemResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierGroupResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierOptionResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ProductResponse;
import com.restaurant.crm.modules.erp.menu.entity.Category;
import com.restaurant.crm.modules.erp.menu.entity.Combo;
import com.restaurant.crm.modules.erp.menu.entity.ComboItem;
import com.restaurant.crm.modules.erp.menu.entity.ModifierGroup;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuManagementMapper {

    @Mapping(target = "branchId", source = "branch.id")
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "categoryId", source = "category.id")
    ProductResponse toProductResponse(Product product);

    @Mapping(target = "productId", source = "product.id")
    ModifierGroupResponse toModifierGroupResponse(ModifierGroup group);

    @Mapping(target = "groupId", source = "modifierGroup.id")
    ModifierOptionResponse toModifierOptionResponse(ModifierOption option);

    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "items", ignore = true)
    ComboResponse toComboResponse(Combo combo);

    @Mapping(target = "comboId", source = "combo.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "modifierOptionIds", ignore = true)
    ComboItemResponse toComboItemResponse(ComboItem item);
}
