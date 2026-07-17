package com.restaurant.crm.common.utils;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public class PagingUtil {

    public static Sort createSort(PagingRequest request) {
        if (request == null) {
            return Sort.unsorted();
        }

        SortRequest sortRequest = request.getSortRequest();
        if (sortRequest == null) {
            return Sort.unsorted();
        }

        if (!StringUtils.hasText(sortRequest.getField())) {
            return Sort.unsorted();
        }

        Sort.Direction direction = Sort.Direction.ASC; // default
        String dirValue = sortRequest.getDirection();
        if (StringUtils.hasText(dirValue)) {
            try {
                direction = Sort.Direction.fromString(dirValue);
            } catch (IllegalArgumentException ignored) {
                // keep default asc if invalid
            }
        }

        return Sort.by(direction, sortRequest.getField());
    }
}
