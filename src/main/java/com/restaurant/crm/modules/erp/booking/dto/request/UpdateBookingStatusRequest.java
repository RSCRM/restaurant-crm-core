package com.restaurant.crm.modules.erp.booking.dto.request;

import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBookingStatusRequest {

    @NotNull(message = "BOOKING_STATUS_REQUIRED")
    BookingStatus status;
}
