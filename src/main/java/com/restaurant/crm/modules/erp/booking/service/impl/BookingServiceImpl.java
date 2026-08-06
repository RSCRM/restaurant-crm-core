package com.restaurant.crm.modules.erp.booking.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.customeraccount.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.erp.booking.dto.request.CreateBookingRequest;
import com.restaurant.crm.modules.erp.booking.dto.request.UpdateBookingStatusRequest;
import com.restaurant.crm.modules.erp.booking.dto.response.BookingResponse;
import com.restaurant.crm.modules.erp.booking.entity.Booking;
import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import com.restaurant.crm.modules.erp.booking.mapper.BookingMapper;
import com.restaurant.crm.modules.erp.booking.repository.BookingRepository;
import com.restaurant.crm.modules.erp.booking.service.interfaces.BookingService;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPoint;
import com.restaurant.crm.modules.crm.pointwallet.repository.CustomerPointRepository;
import com.restaurant.crm.modules.erp.table.entity.TableSession;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
import com.restaurant.crm.modules.erp.table.repository.TableSessionRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {

    BookingRepository bookingRepository;
    CustomerRepository customerRepository;
    OrganizationBranchRepository branchRepository;
    RestaurantTableRepository tableRepository;
    CustomerPointRepository customerPointRepository;
    TableSessionRepository tableSessionRepository;
    BookingMapper bookingMapper;

    private void validateBranchAccess(String targetBranchId) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken)) {
            return;
        }
        String actorUserId = AuthUtils.getCurrentUserId();
        if (AuthUtils.getEmployeeId() == null) {
            branchRepository.findByIdAndOrganization_OwnerId(targetBranchId, actorUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));
        } else if (!targetBranchId.equals(AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        validateBranchAccess(request.getBranchId());

        if (request.getBookingTime() != null && request.getBookingTime().isBefore(java.time.Instant.now())) {
            throw new AppException(ErrorCode.BOOKING_TIME_MUST_BE_FUTURE);
        }

        Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .phone(request.getCustomerPhone())
                        .status(CustomerStatus.ACTIVE)
                        .build()));

        OrganizationBranch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));

        RestaurantTable table = null;
        if (request.getTableId() != null && !request.getTableId().isBlank()) {
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_TABLE_NOT_FOUND));
        }

        Booking booking = bookingMapper.toBooking(request);
        booking.setCustomer(customer);
        booking.setBranch(branch);
        booking.setTables(table);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<BookingResponse> getBookingsByBranch(String branchId, int page, int size) {
        validateBranchAccess(branchId);

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
        
        Page<Booking> bookingPage;
        if (AuthUtils.getEmployeeId() != null) {
            bookingPage = bookingRepository.findByCustomerIdAndBranchId(customerId, AuthUtils.getBranchId(), pageable);
        } else if (AuthUtils.getOrganizationId() != null) {
            bookingPage = bookingRepository.findByCustomerIdAndBranch_OrganizationId(customerId, AuthUtils.getOrganizationId(), pageable);
        } else {
            bookingPage = bookingRepository.findByCustomerId(customerId, pageable);
        }

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
        
        Page<Booking> bookingPage;
        if (AuthUtils.getEmployeeId() != null) {
            bookingPage = bookingRepository.findByCustomerPhoneAndBranchId(phone, AuthUtils.getBranchId(), pageable);
        } else if (AuthUtils.getOrganizationId() != null) {
            bookingPage = bookingRepository.findByCustomerPhoneAndBranch_OrganizationId(phone, AuthUtils.getOrganizationId(), pageable);
        } else {
            bookingPage = bookingRepository.findByCustomerPhone(phone, pageable);
        }

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

        validateBranchAccess(booking.getBranch().getId());

        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(String bookingId, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        validateBranchAccess(booking.getBranch().getId());

        booking.setStatus(request.getStatus());

        if (request.getStatus() == BookingStatus.SEATED) {
            // 1. Ensure Customer is saved and ACTIVE
            Customer customer = booking.getCustomer();
            if (customer != null) {
                if (customer.getStatus() != CustomerStatus.ACTIVE) {
                    customer.setStatus(CustomerStatus.ACTIVE);
                    customerRepository.save(customer);
                }
            }

            // 2. Initialize / Register CustomerPoint wallet for this Organization
            if (customer != null && booking.getBranch() != null && booking.getBranch().getOrganization() != null) {
                String orgId = booking.getBranch().getOrganization().getId();
                Customer finalCustomer = customer;
                customerPointRepository.findByCustomerIdAndOrganizationId(customer.getId(), orgId)
                        .orElseGet(() -> customerPointRepository.save(CustomerPoint.builder()
                                .customer(finalCustomer)
                                .organizationId(orgId)
                                .currentPoints(0)
                                .lifetimePoints(0)
                                .build()));
            }

            // 3. Update assigned Table to OCCUPIED and open TableSession
            RestaurantTable table = booking.getTables();
            if (table != null) {
                table.setStatus(RestaurantTableStatus.OCCUPIED);
                tableRepository.save(table);

                if (!tableSessionRepository.existsByTableIdAndStatus(table.getId(), TableSessionStatus.ACTIVE)) {
                    String guestPhone = (customer != null) ? customer.getPhone() : "N/A";
                    tableSessionRepository.save(TableSession.builder()
                            .branchId(booking.getBranch().getId())
                            .table(table)
                            .guestName("Khách đặt bàn")
                            .guestPhone(guestPhone)
                            .partySize(booking.getGuestCount() != null ? booking.getGuestCount() : 1)
                            .status(TableSessionStatus.ACTIVE)
                            .startedAt(java.time.Instant.now())
                            .note(booking.getNote())
                            .build());
                }
            }
        }

        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingResponse(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<BookingResponse> searchBookings(com.restaurant.crm.modules.erp.booking.dto.request.BookingSearchRequest searchRequest, int page, int size) {
        if (searchRequest != null) {
            if ((searchRequest.getBranchId() == null || searchRequest.getBranchId().isBlank()) && AuthUtils.getBranchId() != null) {
                searchRequest.setBranchId(AuthUtils.getBranchId());
            }
            if (searchRequest.getBranchId() != null && !searchRequest.getBranchId().isBlank()) {
                validateBranchAccess(searchRequest.getBranchId());
            }
        }

        Pageable pageable = PageRequest.of(page - GlobalVariableConstant.PAGE_SIZE_INDEX, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        Page<Booking> bookingPage = bookingRepository.findAll(com.restaurant.crm.modules.erp.booking.specification.BookingSpecification.build(searchRequest), pageable);

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
    @Transactional
    public BookingResponse updateBooking(String id, com.restaurant.crm.modules.erp.booking.dto.request.UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        validateBranchAccess(booking.getBranch().getId());

        if (request.getBookingTime() != null && request.getBookingTime().isBefore(java.time.Instant.now())) {
            throw new AppException(ErrorCode.BOOKING_TIME_MUST_BE_FUTURE);
        }

        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                    .orElseGet(() -> customerRepository.save(Customer.builder()
                            .phone(request.getCustomerPhone())
                            .status(CustomerStatus.ACTIVE)
                            .build()));
            booking.setCustomer(customer);
        }

        if (request.getTableId() != null) {
            if (request.getTableId().isBlank()) {
                booking.setTables(null);
            } else {
                RestaurantTable table = tableRepository.findById(request.getTableId())
                        .orElseThrow(() -> new AppException(ErrorCode.ORDER_TABLE_NOT_FOUND));
                booking.setTables(table);
            }
        }

        if (request.getBookingTime() != null) {
            booking.setBookingTime(request.getBookingTime());
        }

        if (request.getGuestCount() != null) {
            booking.setGuestCount(request.getGuestCount());
        }

        if (request.getNote() != null) {
            booking.setNote(request.getNote());
        }

        if (request.getStatus() != null) {
            booking.setStatus(request.getStatus());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(updatedBooking);
    }
}
