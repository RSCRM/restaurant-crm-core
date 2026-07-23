package com.restaurant.crm.modules.erp.booking.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.crm.customer_account.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.customer_account.repository.CustomerRepository;
import com.restaurant.crm.modules.erp.booking.dto.request.CreateBookingRequest;
import com.restaurant.crm.modules.erp.booking.dto.request.UpdateBookingStatusRequest;
import com.restaurant.crm.modules.erp.booking.dto.response.BookingResponse;
import com.restaurant.crm.modules.erp.booking.entity.Booking;
import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import com.restaurant.crm.modules.erp.booking.mapper.BookingMapper;
import com.restaurant.crm.modules.erp.booking.repository.BookingRepository;
import com.restaurant.crm.modules.erp.booking.service.interfaces.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {

    BookingRepository bookingRepository;
    CustomerRepository customerRepository;
    BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .phone(request.getCustomerPhone())
                        .status(CustomerStatus.ACTIVE)
                        .build()));

        Booking booking = bookingMapper.toBooking(request);
        booking.setCustomer(customer);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<BookingResponse> getBookingsByBranch(String branchId, int page, int size) {
        Pageable pageable = PageRequest.of(page - GlobalVariableConstant.PAGE_SIZE_INDEX, size);
        Page<Booking> bookingPage = bookingRepository.findByBranchId(branchId, pageable);

        return PagingResponse.<BookingResponse>builder()
                .currentPage(page)
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalElement(bookingPage.getTotalElements())
                .data(bookingPage.getContent().stream()
                        .map(bookingMapper::toBookingResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<BookingResponse> getBookingsByCustomer(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page - GlobalVariableConstant.PAGE_SIZE_INDEX, size);
        Page<Booking> bookingPage = bookingRepository.findByCustomerId(customerId, pageable);

        return PagingResponse.<BookingResponse>builder()
                .currentPage(page)
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalElement(bookingPage.getTotalElements())
                .data(bookingPage.getContent().stream()
                        .map(bookingMapper::toBookingResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<BookingResponse> getBookingsByCustomerPhone(String phone, int page, int size) {
        Pageable pageable = PageRequest.of(page - GlobalVariableConstant.PAGE_SIZE_INDEX, size);
        Page<Booking> bookingPage = bookingRepository.findByCustomerPhone(phone, pageable);

        return PagingResponse.<BookingResponse>builder()
                .currentPage(page)
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalElement(bookingPage.getTotalElements())
                .data(bookingPage.getContent().stream()
                        .map(bookingMapper::toBookingResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(String bookingId, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        booking.setStatus(request.getStatus());
        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingResponse(updatedBooking);
    }
}
