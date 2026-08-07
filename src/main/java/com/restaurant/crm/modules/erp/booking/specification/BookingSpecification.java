package com.restaurant.crm.modules.erp.booking.specification;

import com.restaurant.crm.modules.erp.booking.dto.request.BookingSearchRequest;
import com.restaurant.crm.modules.erp.booking.entity.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookingSpecification {

    private BookingSpecification() {}

    public static Specification<Booking> build(BookingSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // Branch filter
            if (request.getBranchId() != null && !request.getBranchId().isBlank()) {
                predicates.add(cb.equal(root.get("branch").get("id"), request.getBranchId()));
            }

            // ── Search Keyword (OR across phone, customer name, table number, note) ──
            if (request.getSearchKeyword() != null && !request.getSearchKeyword().isBlank()) {
                String pattern = "%" + request.getSearchKeyword().trim().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                // Customer Phone
                searchPredicates.add(cb.like(cb.lower(root.get("customer").get("phone")), pattern));

                // Customer FullName (if customer has fullName field)
                try {
                    searchPredicates.add(cb.like(cb.lower(root.get("customer").get("fullName")), pattern));
                } catch (Exception ignored) {}

                // Table number
                try {
                    searchPredicates.add(cb.like(cb.lower(root.get("tables").get("tableNumber")), pattern));
                } catch (Exception ignored) {}

                // Note
                searchPredicates.add(cb.like(cb.lower(root.get("note")), pattern));

                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            // ── Filter fields (AND) ──
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getMinGuests() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("guestCount"), request.getMinGuests()));
            }

            if (request.getMaxGuests() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("guestCount"), request.getMaxGuests()));
            }

            if (request.getBookingTimeFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bookingTime"), request.getBookingTimeFrom()));
            }

            if (request.getBookingTimeTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bookingTime"), request.getBookingTimeTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
