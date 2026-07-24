package com.restaurant.crm.modules.erp.inventory.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.inventory.constants.IngredientConstants;
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
@Table(name = IngredientConstants.TABLE_INGREDIENT)
public class Ingredient extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = IngredientConstants.COL_BRANCH_ID,
            nullable = false
    )
    OrganizationBranch branch;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = IngredientConstants.COL_INGREDIENT_CATEGORY_ID,
            nullable = false
    )
    IngredientCategory ingredientCategory;

    @NotBlank
    @Size(max = IngredientConstants.MAX_CHARS_INGREDIENT_NAME)
    @Column(
            name = IngredientConstants.COL_INGREDIENT_NAME,
            nullable = false,
            columnDefinition = IngredientConstants.INGREDIENT_NAME_DEFINITION
    )
    String ingredientName;

    @NotBlank
    @Size(max = IngredientConstants.MAX_CHARS_UNIT)
    @Column(
            name = IngredientConstants.COL_UNIT,
            nullable = false,
            columnDefinition = IngredientConstants.UNIT_DEFINITION
    )
    String unit;

    @Size(max = IngredientConstants.MAX_CHARS_DESCRIPTION)
    @Column(
            name = IngredientConstants.COL_DESCRIPTION,
            columnDefinition = IngredientConstants.DESCRIPTION_DEFINITION
    )
    String description;
}