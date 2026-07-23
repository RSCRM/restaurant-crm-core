package com.restaurant.crm.modules.erp.table.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.table.constants.TableMapConstants;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
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
        name = "restaurant_tables",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_restaurant_tables_area_table_number", columnNames = {"area_id", "table_number"})
        }
)
@AttributeOverride(name = "id", column = @Column(name = "table_id", columnDefinition = "VARCHAR(36)"))
public class RestaurantTable extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    TableArea area;

    @NotBlank
    @NotNull
    @Size(max = 20)
    @Column(name = "table_number", nullable = false, length = 20)
    String tableNumber;

    @NotNull
    @Min(1)
    @Column(name = "capacity", nullable = false)
    Integer capacity;

    @lombok.Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    RestaurantTableStatus status = RestaurantTableStatus.AVAILABLE;

    @Column(name = TableMapConstants.COL_POSITION_X)
    Integer positionX;

    @Column(name = TableMapConstants.COL_POSITION_Y)
    Integer positionY;
}
