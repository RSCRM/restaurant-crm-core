package com.restaurant.crm.modules.erp.menu.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Table(name = "modifier_options", uniqueConstraints = {
        @UniqueConstraint(name = "uk_modifier_options_group_option_name", columnNames = {"modifier_group_id", "option_name"})
})
@AttributeOverride(name = "id", column = @Column(name = "modifier_option_id", columnDefinition = "VARCHAR(36)"))
public class ModifierOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_group_id", nullable = false)
    ModifierGroup modifierGroup;

    @NotBlank
    @NotNull
    @Size(max = 100)
    @Column(name = "option_name", nullable = false, length = 100)
    String optionName;

    @Builder.Default
    @NotNull
    @Column(name = "additional_price", nullable = false, precision = 10, scale = 2)
    BigDecimal additionalPrice = BigDecimal.ZERO;

    @Builder.Default
    @NotBlank
    @NotNull
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    String status = "AVAILABLE";
}
