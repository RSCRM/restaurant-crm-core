package com.restaurant.crm.modules.erp.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateModifierGroupRequest {
    @NotBlank(message = "MODIFIER_GROUP_NAME_REQUIRED")
    @Size(max = 100)
    String groupName;
    @Size(max = 255)
    String description;
    @NotNull
    @Min(0)
    Integer minSelection;
    @NotNull
    @Min(0)
    Integer maxSelection;
}
