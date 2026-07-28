package com.restaurant.crm.modules.erp.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComboItemRequest {
    @NotBlank(message = "COMBO_ITEM_PRODUCT_REQUIRED")
    String productId;
    @NotNull
    @Min(1)
    Integer quantity;
    // 1 option moi group cua product; so luong = so group cua product (0 group -> rong)
    List<String> modifierOptionIds;
}
