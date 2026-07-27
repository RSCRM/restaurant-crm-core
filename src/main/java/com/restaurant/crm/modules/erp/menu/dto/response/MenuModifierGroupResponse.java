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

/**
 * A modifier group in the customer menu (uc-c-04), returned at menu level (not per product,
 * because there is no Product↔ModifierGroup join table yet — TODO uc-c-04).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuModifierGroupResponse {

    String modifierGroupId;
    String groupName;
    String description;
    Integer minSelection;
    Integer maxSelection;

    @Builder.Default
    List<MenuModifierOptionResponse> options = new ArrayList<>();
}
