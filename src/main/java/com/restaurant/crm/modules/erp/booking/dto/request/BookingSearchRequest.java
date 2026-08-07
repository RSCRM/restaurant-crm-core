package com.restaurant.crm.modules.erp.booking.dto.request;

import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
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
public class BookingSearchRequest {

    String branchId;

    String searchKeyword;

    BookingStatus status;

    Integer minGuests;

    Integer maxGuests;

    Instant bookingTimeFrom;

    Instant bookingTimeTo;
}
