package com.restaurant.crm.modules.erp.booking.mapper;

import com.restaurant.crm.modules.erp.booking.dto.request.CreateBookingRequest;
import com.restaurant.crm.modules.erp.booking.dto.response.BookingResponse;
import com.restaurant.crm.modules.erp.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "tables", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Booking toBooking(CreateBookingRequest request);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.phone", target = "customerPhone")
    @Mapping(source = "branch.id", target = "branchId")
    @Mapping(source = "tables.id", target = "tableId")
    BookingResponse toBookingResponse(Booking booking);
}
