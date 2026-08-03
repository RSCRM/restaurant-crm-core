package com.restaurant.crm.modules.erp.organization.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalaryConfigRequest {

    @NotNull(message = "EMPLOYEE_SALARY_INVALID")
    @PositiveOrZero(message = "EMPLOYEE_SALARY_INVALID")
    BigDecimal salary;
}
