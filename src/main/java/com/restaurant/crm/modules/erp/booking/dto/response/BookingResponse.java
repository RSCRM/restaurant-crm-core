package com.restaurant.crm.modules.erp.booking.dto.response;

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
public class BookingResponse {

    String id;
    String branchId;
    String tableId;
    String customerId;
    String customerPhone;
    Instant bookingTime;
    Integer guestCount;
    BookingStatus status;
    String note;
    Instant createdAt;
    Instant updatedAt;
}
