package com.restaurant.crm.modules.erp.organization.dto.request;

import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEmployeeRequest {
    @NotNull(message = "EMPLOYEE_EMAIL_REQUIRED")
    @Email(message = "EMPLOYEE_EMAIL_INVALID")
    @Size(max = 100)
    String email;
    @Size(max = 20)
    String phone;
    EmployeeStatus status;
    @NotNull(message = "EMPLOYEE_START_DATE_REQUIRED")
    LocalDate startDate;
    LocalDate endDate;
}
