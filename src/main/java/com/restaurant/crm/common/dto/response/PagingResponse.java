package com.restaurant.crm.common.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagingResponse<T> {
    int currentPage;
    int pageSize;
    int totalPages;
    long totalElement;

    @Builder.Default
    List<T> data = Collections.emptyList();
}
