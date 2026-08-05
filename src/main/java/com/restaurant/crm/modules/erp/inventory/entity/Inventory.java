package com.restaurant.crm.modules.erp.inventory.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.inventory.constants.InventoryConstants;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = InventoryConstants.COL_BRANCH_ID,
        nullable = false
    )
    OrganizationBranch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = InventoryConstants.COL_INVENTORY_CATEGORY_ID,
        nullable = false
    )
    InventoryCategory inventoryCategory;

    @NotBlank
    @Size(max = InventoryConstants.MAX_CHARS_INVENTORY_NAME)
    @Column(
        name = InventoryConstants.COL_INVENTORY_NAME,
        nullable = false,
        columnDefinition = InventoryConstants.INVENTORY_NAME_DEFINITION
    )
    String inventoryName;

    @NotBlank
    @Size(max = InventoryConstants.MAX_CHARS_UNIT)
    @Column(
        name = InventoryConstants.COL_UNIT,
        nullable = false,
        columnDefinition = InventoryConstants.UNIT_DEFINITION
    )
    String unit;

    @Size(max = InventoryConstants.MAX_CHARS_DESCRIPTION)
    @Column(
        name = InventoryConstants.COL_DESCRIPTION,
        columnDefinition = InventoryConstants.DESCRIPTION_DEFINITION
    )
    String description;

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