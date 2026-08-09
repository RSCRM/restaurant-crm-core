package com.restaurant.crm.modules.erp.booking.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.booking.dto.request.CreateBookingRequest;
import com.restaurant.crm.modules.erp.booking.dto.request.UpdateBookingStatusRequest;
import com.restaurant.crm.modules.erp.booking.dto.request.UpdateBookingRequest;
import com.restaurant.crm.modules.erp.booking.dto.response.BookingResponse;
import com.restaurant.crm.modules.erp.booking.service.interfaces.BookingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crm/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder().data(response).build());
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    public ResponseEntity<ApiResponse<PagingResponse<BookingResponse>>> getBookingsByBranch(
            @PathVariable String branchId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<BookingResponse> response = bookingService.getBookingsByBranch(branchId, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<BookingResponse>>builder().data(response).build());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    public ResponseEntity<ApiResponse<PagingResponse<BookingResponse>>> getBookingsByCustomer(
            @PathVariable String customerId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<BookingResponse> response = bookingService.getBookingsByCustomer(customerId, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<BookingResponse>>builder().data(response).build());
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    public ResponseEntity<ApiResponse<PagingResponse<BookingResponse>>> getBookingsByCustomerPhone(
            @PathVariable String phone,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<BookingResponse> response = bookingService.getBookingsByCustomerPhone(phone, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<BookingResponse>>builder().data(response).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable String id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder().data(response).build());
    }


    @PostMapping("/search")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    public ResponseEntity<ApiResponse<PagingResponse<BookingResponse>>> searchBookings(
            @RequestBody com.restaurant.crm.modules.erp.booking.dto.request.BookingSearchRequest searchRequest,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<BookingResponse> response = bookingService.searchBookings(searchRequest, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<BookingResponse>>builder().data(response).build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBookingStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingStatusRequest request
    ) {
        BookingResponse response = bookingService.updateBookingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder().data(response).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable String id,
            @Valid @RequestBody UpdateBookingRequest request
    ) {
        BookingResponse response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder().data(response).build());
    }
}
