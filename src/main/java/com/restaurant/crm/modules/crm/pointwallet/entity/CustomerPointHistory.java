package com.restaurant.crm.modules.crm.pointwallet.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.pointwallet.constants.CustomerPointHistoryConstants;
import com.restaurant.crm.modules.crm.pointwallet.enums.PointTransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@Table(name = CustomerPointHistoryConstants.TABLE_CUSTOMER_POINT_HISTORY)
public class CustomerPointHistory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CustomerPointHistoryConstants.COL_CUSTOMER_ID, nullable = false)
    Customer customer;

    @NotNull
    @Column(name = CustomerPointHistoryConstants.COL_RESTAURANT_ID, nullable = false)
    String restaurantId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = CustomerPointHistoryConstants.COL_TRANSACTION_TYPE, nullable = false)
    PointTransactionType transactionType;

    @NotNull
    @Column(name = CustomerPointHistoryConstants.COL_POINTS_CHANGED, nullable = false)
    Integer pointsChanged;

    @Column(name = CustomerPointHistoryConstants.COL_REFERENCE_ID)
    String referenceId;
}
