package com.restaurant.crm.modules.erp.inventory.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.inventory.constants.InventoryCategoryConstants;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryCategoryStatus;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = InventoryCategoryConstants.TABLE_INVENTORY_CATEGORY)
public class InventoryCategory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = InventoryCategoryConstants.COL_BRANCH_ID,
            nullable = false
    )
    OrganizationBranch branch;

    @NotBlank
    @Size(max = InventoryCategoryConstants.MAX_CHARS_CATEGORY_NAME)
    @Column(
            name = InventoryCategoryConstants.COL_CATEGORY_NAME,
            nullable = false,
            columnDefinition = InventoryCategoryConstants.CATEGORY_NAME_DEFINITION
    )
    String categoryName;

    @Size(max = InventoryCategoryConstants.MAX_CHARS_DESCRIPTION)
    @Column(
            name = InventoryCategoryConstants.COL_DESCRIPTION,
            columnDefinition = InventoryCategoryConstants.DESCRIPTION_DEFINITION
    )
    String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
        name = InventoryCategoryConstants.COL_STATUS,
        nullable = false,
        columnDefinition = InventoryCategoryConstants.STATUS_DEFINITION
    )
    InventoryCategoryStatus status = InventoryCategoryStatus.ACTIVE;
}