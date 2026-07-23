package com.restaurant.crm.modules.licensemanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GrantSubscriptionRequest {

    @NotBlank(message = "ORGANIZATION_NOT_FOUND")
    String organizationId;

    @NotBlank(message = "LICENSE_NOT_FOUND")
    String licenseId;

    /** Optional; if null, service will set = LocalDate.now() */
    LocalDate startDate;
}
