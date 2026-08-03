package com.restaurant.crm.modules.erp.table.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.table.constants.TableSessionConstants;
import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
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

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = TableSessionConstants.TABLE_NAME,
        indexes = {
                @Index(name = "idx_table_sessions_branch_status", columnList = "branch_id,status"),
                @Index(name = "idx_table_sessions_table_status", columnList = "table_id,status")
        }
)
@AttributeOverride(
        name = "id",
        column = @Column(name = TableSessionConstants.ID_COLUMN, columnDefinition = "VARCHAR(36)")
)
public class TableSession extends BaseEntity {

    @NotBlank
    @Column(name = TableSessionConstants.BRANCH_ID_COLUMN, nullable = false, columnDefinition = "VARCHAR(36)")
    String branchId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = TableSessionConstants.TABLE_ID_COLUMN, nullable = false)
    RestaurantTable table;

    @NotBlank
    @Size(max = TableSessionConstants.MAX_GUEST_NAME_LENGTH)
    @Column(
            name = TableSessionConstants.GUEST_NAME_COLUMN,
            nullable = false,
            length = TableSessionConstants.MAX_GUEST_NAME_LENGTH
    )
    String guestName;

    @Size(max = TableSessionConstants.MAX_GUEST_PHONE_LENGTH)
    @Column(name = TableSessionConstants.GUEST_PHONE_COLUMN, length = TableSessionConstants.MAX_GUEST_PHONE_LENGTH)
    String guestPhone;

    @NotNull
    @Min(1)
    @Column(name = TableSessionConstants.PARTY_SIZE_COLUMN, nullable = false)
    Integer partySize;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = TableSessionConstants.STATUS_COLUMN, nullable = false, length = 20)
    TableSessionStatus status = TableSessionStatus.ACTIVE;

    @NotNull
    @Column(name = TableSessionConstants.STARTED_AT_COLUMN, nullable = false)
    Instant startedAt;

    @Column(name = TableSessionConstants.ENDED_AT_COLUMN)
    Instant endedAt;

    @Size(max = TableSessionConstants.MAX_NOTE_LENGTH)
    @Column(name = TableSessionConstants.NOTE_COLUMN, length = TableSessionConstants.MAX_NOTE_LENGTH)
    String note;
}
