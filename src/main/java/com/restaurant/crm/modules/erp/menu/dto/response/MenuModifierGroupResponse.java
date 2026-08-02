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
public class MenuModifierGroupResponse {

    String modifierGroupId;
    String groupName;
    String description;
    Integer minSelection;
    Integer maxSelection;

    @Builder.Default
    List<MenuModifierOptionResponse> options = new ArrayList<>();
}
