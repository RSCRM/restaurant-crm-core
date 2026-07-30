package com.restaurant.crm.modules.erp.organization.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateAccessRequest {

    @NotNull
    Boolean enabled;
}
