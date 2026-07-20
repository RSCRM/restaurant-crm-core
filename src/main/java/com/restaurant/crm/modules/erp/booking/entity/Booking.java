package com.restaurant.crm.modules.erp.booking.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.erp.booking.constants.BookingConstants;
import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
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
@Table(name = BookingConstants.TABLE_NAME)
public class Booking extends BaseEntity {

    // TODO: Refactor to @ManyToOne when Branch module/entity is initialized.
    // FK: FK_BOOKINGS_BRANCH_ID -> branches(id)
    @NotNull
    @Column(name = BookingConstants.COL_BRANCH_ID, nullable = false)
    String branchId;

    // TODO: Refactor to @ManyToOne when Restaurant Table module/entity is initialized.
    // FK: FK_BOOKINGS_TABLE_ID -> restaurant_tables(id)
    @Column(name = BookingConstants.COL_TABLE_ID)
    String tableId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = BookingConstants.COL_CUSTOMER_ID, nullable = false)
    Customer customer;

    @NotNull
    @Column(name = BookingConstants.COL_BOOKING_TIME, nullable = false)
    Instant bookingTime;

    @NotNull
    @Min(BookingConstants.MIN_GUEST_COUNT)
    @Column(name = BookingConstants.COL_GUEST_COUNT, nullable = false)
    Integer guestCount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = BookingConstants.COL_STATUS, nullable = false)
    BookingStatus status = BookingStatus.PENDING;

    @Size(max = BookingConstants.MAX_NOTE_LENGTH)
    @Column(name = BookingConstants.COL_NOTE)
    String note;
}
