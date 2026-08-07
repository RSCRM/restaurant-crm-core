package com.restaurant.crm.modules.erp.booking.dto.request;

import com.restaurant.crm.modules.crm.customeraccount.constants.CustomerConstants;
import com.restaurant.crm.modules.erp.booking.constants.BookingConstants;
import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBookingRequest {

    String branchId;

    String tableId;

    @Size(min = CustomerConstants.MIN_CHARS_PHONE, max = CustomerConstants.MAX_CHARS_PHONE, message = "CUSTOMER_PHONE_INVALID_LENGTH")
    String customerPhone;

    Instant bookingTime;

    @Min(value = 1, message = "GUEST_COUNT_MUST_BE_GREATER_THAN_ZERO")
    Integer guestCount;

    @Size(max = BookingConstants.MAX_NOTE_LENGTH, message = "NOTE_EXCEEDS_MAX_LENGTH")
    String note;

    BookingStatus status;
}
