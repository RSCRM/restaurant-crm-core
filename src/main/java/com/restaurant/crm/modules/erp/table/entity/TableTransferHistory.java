package com.restaurant.crm.modules.erp.table.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.table.constants.TableSessionConstants;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = TableSessionConstants.TRANSFER_HISTORY_TABLE,
        indexes = @Index(name = "idx_table_transfer_history_session", columnList = "table_session_id")
)
@AttributeOverride(
        name = "id",
        column = @Column(name = TableSessionConstants.TRANSFER_HISTORY_ID_COLUMN, columnDefinition = "VARCHAR(36)")
)
public class TableTransferHistory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableSessionConstants.SESSION_ID_COLUMN, nullable = false)
    TableSession session;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableSessionConstants.SOURCE_TABLE_ID_COLUMN, nullable = false)
    RestaurantTable sourceTable;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableSessionConstants.TARGET_TABLE_ID_COLUMN, nullable = false)
    RestaurantTable targetTable;

    @Column(name = TableSessionConstants.TRANSFERRED_BY_COLUMN, columnDefinition = "VARCHAR(36)")
    String transferredBy;

    @NotNull
    @Column(name = TableSessionConstants.TRANSFERRED_AT_COLUMN, nullable = false)
    Instant transferredAt;
}
