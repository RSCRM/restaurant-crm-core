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
 * A menu category grouping products (uc-c-04).
 * {@code categoryName} is always null: {@code Product.categoryId} is a bare String with no
 * Category entity yet (TODO uc-c-04 — when a Category entity lands, populate the name).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuCategoryResponse {

    String categoryId;
    String categoryName;

    @Builder.Default
    List<MenuProductResponse> products = new ArrayList<>();
}
