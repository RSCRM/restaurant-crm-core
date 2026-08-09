package com.restaurant.crm.modules.erp.organization.constants;

import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;

public final class EmployeeAccountConstants {
    private EmployeeAccountConstants() {}

    public static final String DEFAULT_PASSWORD = "Employee@123";

    // Employee moi chua co org role nen chua the ACTIVE, activate sau khi assign role
    public static final EmployeeStatus DEFAULT_STATUS = EmployeeStatus.INACTIVE;
}
