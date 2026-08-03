package com.restaurant.crm.modules.licensemanagement.enums;

import lombok.Getter;

@Getter
public enum BillingCycle {
    MONTHLY(30),
    YEARLY(365);

    private final int days;

    BillingCycle(int days) {
        this.days = days;
    }
}
