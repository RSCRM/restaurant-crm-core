package com.restaurant.crm.modules.erp.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.constants.OrderConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * TEMPORARY STUB — owner: SangTD6 (uc-sw-*). Replace with the real Order entity.
 * Only the minimal fields the kitchen-display read path (uc-scf-01) needs are declared here:
 * {@code branchId} for NFR-07 isolation and {@code tableNumber} for the card. Do not build
 * order-lifecycle logic on this class; it exists so uc-scf-01 can compile and run.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = OrderConstants.TABLE_ORDER)
public class Order extends BaseEntity {

    @Column(name = OrderConstants.COL_BRANCH_ID, columnDefinition = OrderConstants.BRANCH_ID_DEFINITION)
    String branchId;

    @Column(name = OrderConstants.COL_TABLE_NUMBER, columnDefinition = OrderConstants.TABLE_NUMBER_DEFINITION)
    String tableNumber;
}
