package com.restaurant.crm.modules.erp.inventory.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.inventory.constants.InventoryTransactionConstants;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = InventoryTransactionConstants.TABLE_INVENTORY_TRANSACTION)
public class InventoryTransaction extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = InventoryTransactionConstants.COL_INVENTORY_ID,
        nullable = false
    )
    Inventory inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = InventoryTransactionConstants.COL_EMPLOYEE_ID
    )
    Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = InventoryTransactionConstants.COL_TRANSACTION_TYPE,
        nullable = false
    )
    InventoryTransactionType transactionType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
        name = InventoryTransactionConstants.COL_TRANSACTION_DIRECTION,
        nullable = false
    )
    InventoryTransactionDirection transactionDirection;

    @NotNull
    @Column(
        name = InventoryTransactionConstants.COL_QUANTITY,
        nullable = false,
        columnDefinition = InventoryTransactionConstants.QUANTITY_DEFINITION
    )
    BigDecimal quantity;

    @Column(
        name = InventoryTransactionConstants.COL_NOTE,
        columnDefinition = InventoryTransactionConstants.NOTE_DEFINITION
    )
    String note;

    @Column(
        name = InventoryTransactionConstants.COL_REFERENCE_ID,
        columnDefinition = InventoryTransactionConstants.REFERENCE_ID_DEFINITION
    )
    String referenceId;

    @NotNull
    @Column(
        name = InventoryTransactionConstants.COL_TRANSACTION_TIME,
        nullable = false
    )
    Instant transactionTime;
}