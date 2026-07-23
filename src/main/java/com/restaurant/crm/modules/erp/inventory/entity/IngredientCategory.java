package com.restaurant.crm.modules.erp.inventory.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.inventory.constants.IngredientCategoryConstants;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = IngredientCategoryConstants.TABLE_INGREDIENT_CATEGORY)
public class IngredientCategory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = IngredientCategoryConstants.COL_BRANCH_ID,
            nullable = false
    )
    OrganizationBranch branch;

    @NotBlank
    @Size(max = IngredientCategoryConstants.MAX_CHARS_CATEGORY_NAME)
    @Column(
            name = IngredientCategoryConstants.COL_CATEGORY_NAME,
            nullable = false,
            columnDefinition = IngredientCategoryConstants.CATEGORY_NAME_DEFINITION
    )
    String categoryName;

    @Size(max = IngredientCategoryConstants.MAX_CHARS_DESCRIPTION)
    @Column(
            name = IngredientCategoryConstants.COL_DESCRIPTION,
            columnDefinition = IngredientCategoryConstants.DESCRIPTION_DEFINITION
    )
    String description;
}