package com.restaurant.crm.modules.erp.table.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.table.constants.TableMapConstants;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "table_areas",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_table_areas_branch_area_name", columnNames = {"branch_id", "area_name"})
        }
)
@AttributeOverride(name = "id", column = @Column(name = "area_id", columnDefinition = "VARCHAR(36)"))
public class TableArea extends BaseEntity {

    @NotBlank
    @NotNull
    @Column(name = "branch_id", nullable = false, columnDefinition = "VARCHAR(36)")
    String branchId;

    @NotBlank
    @NotNull
    @Size(max = 100)
    @Column(name = "area_name", nullable = false, length = 100)
    String areaName;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    String description;

    @Column(name = TableMapConstants.COL_DISPLAY_ORDER)
    Integer displayOrder;
}
