package com.restaurant.crm.modules.erp.inventory.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.inventory.constants.InventoryConstants;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = InventoryConstants.TABLE_INVENTORY)
public class Inventory extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = InventoryConstants.COL_INGREDIENT_ID,
        nullable = false,
        unique = true
    )
    Ingredient ingredient;

    @Builder.Default
    @Column(
        name = InventoryConstants.COL_QUANTITY,
        nullable = false,
        columnDefinition = InventoryConstants.QUANTITY_DEFINITION
    )
    BigDecimal quantity = BigDecimal.ZERO;

    @Builder.Default
    @Column(
        name = InventoryConstants.COL_MINIMUM_QUANTITY,
        nullable = false,
        columnDefinition = InventoryConstants.QUANTITY_DEFINITION
    )
    BigDecimal minimumQuantity = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
        name = InventoryConstants.COL_STATUS,
        nullable = false,
        columnDefinition = InventoryConstants.STATUS_DEFINITION
    )
    InventoryStatus status = InventoryStatus.GOOD;
}