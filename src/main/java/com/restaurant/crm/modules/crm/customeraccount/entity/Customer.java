package com.restaurant.crm.modules.crm.customeraccount.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.crm.customeraccount.constants.CustomerConstants;
import com.restaurant.crm.modules.crm.customeraccount.enums.CustomerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = CustomerConstants.TABLE_CUSTOMER)
public class Customer extends BaseEntity {

    @NotBlank
    @NotNull
    @Column(name = CustomerConstants.COL_PHONE, nullable = false, unique = true, columnDefinition = CustomerConstants.PHONE_DEFINITION)
    @Size(min = CustomerConstants.MIN_CHARS_PHONE, max = CustomerConstants.MAX_CHARS_PHONE)
    String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = CustomerConstants.COL_STATUS, nullable = false)
    CustomerStatus status = CustomerStatus.ACTIVE;
}
