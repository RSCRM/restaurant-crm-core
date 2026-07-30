package com.restaurant.crm.modules.erp.order.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/** Per-stage item counts for the tracking view (uc-c-06); {@code total} = sum of all lines. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerOrderTrackingSummaryResponse {

    Integer total;
    Integer received;
    Integer cooking;
    Integer readyToServe;
    Integer served;
    Integer cancelled;
}
