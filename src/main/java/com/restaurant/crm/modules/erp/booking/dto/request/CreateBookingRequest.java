package com.restaurant.crm.modules.erp.booking.dto.request;

import com.restaurant.crm.modules.crm.customeraccount.constants.CustomerConstants;
import com.restaurant.crm.modules.erp.booking.constants.BookingConstants;
import jakarta.validation.constraints.Future;
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

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateBookingRequest {

    @NotBlank(message = "BRANCH_ID_REQUIRED")
    String branchId;

    String tableId;

    @NotBlank(message = "CUSTOMER_PHONE_REQUIRED")
    @Size(min = CustomerConstants.MIN_CHARS_PHONE, max = CustomerConstants.MAX_CHARS_PHONE, message = "CUSTOMER_PHONE_INVALID_LENGTH")
    String customerPhone;

    @NotNull(message = "BOOKING_TIME_REQUIRED")
    @Future(message = "BOOKING_TIME_MUST_BE_FUTURE")
    Instant bookingTime;

    @NotNull(message = "GUEST_COUNT_REQUIRED")
    @Min(value = BookingConstants.MIN_GUEST_COUNT, message = "MIN_GUEST_COUNT_INVALID")
    Integer guestCount;

    @Size(max = BookingConstants.MAX_NOTE_LENGTH, message = "NOTE_EXCEEDS_MAX_LENGTH")
    String note;
}
