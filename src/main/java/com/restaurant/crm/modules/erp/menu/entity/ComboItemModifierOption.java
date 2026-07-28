package com.restaurant.crm.modules.erp.menu.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "combo_item_modifier_options", uniqueConstraints = {
        @UniqueConstraint(name = "uk_combo_item_modifier_options_item_option", columnNames = {"combo_item_id", "modifier_option_id"})
})
@AttributeOverride(name = "id", column = @Column(name = "combo_item_modifier_option_id", columnDefinition = "VARCHAR(36)"))
public class ComboItemModifierOption extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_item_id", nullable = false)
    ComboItem comboItem;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_option_id", nullable = false)
    ModifierOption modifierOption;
}
