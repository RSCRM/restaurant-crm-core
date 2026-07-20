package com.restaurant.crm.modules.erp.booking.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.booking.dto.request.CreateBookingRequest;
import com.restaurant.crm.modules.erp.booking.dto.request.UpdateBookingStatusRequest;
import com.restaurant.crm.modules.erp.booking.dto.response.BookingResponse;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    PagingResponse<BookingResponse> getBookingsByBranch(String branchId, int page, int size);

    PagingResponse<BookingResponse> getBookingsByCustomer(String customerId, int page, int size);

    PagingResponse<BookingResponse> getBookingsByCustomerPhone(String phone, int page, int size);

    BookingResponse getBookingById(String id);

    BookingResponse updateBookingStatus(String bookingId, UpdateBookingStatusRequest request);
}
