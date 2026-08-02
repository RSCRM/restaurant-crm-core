package com.restaurant.crm.modules.erp.menu.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuComboResponse {

    String comboId;
    String comboName;
    String description;
    BigDecimal price;
    String imageUrl;
    boolean available;

    /** Component products of the combo — empty until a Combo↔Product join exists (TODO uc-c-04). */
    @Builder.Default
    List<MenuProductResponse> items = new ArrayList<>();
}
