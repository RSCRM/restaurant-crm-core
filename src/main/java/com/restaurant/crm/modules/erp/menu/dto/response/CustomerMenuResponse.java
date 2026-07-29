package com.restaurant.crm.modules.erp.menu.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerMenuResponse {

    String branchId;

    @Builder.Default
    List<MenuCategoryResponse> categories = new ArrayList<>();

    @Builder.Default
    List<MenuComboResponse> combos = new ArrayList<>();

    @Builder.Default
    List<MenuModifierGroupResponse> modifierGroups = new ArrayList<>();
}
