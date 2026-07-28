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
 * The full digital menu of a branch returned in one shot (uc-c-04) — no pagination, no N+1,
 * so the client can render everything in a single request (NFR-09).
 */
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
