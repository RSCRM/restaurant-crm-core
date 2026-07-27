package com.restaurant.crm.modules.erp.table.dto.response;

import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableSessionResponse {
    String id;
    String branchId;
    String tableId;
    String tableNumber;
    String guestName;
    String guestPhone;
    Integer partySize;
    TableSessionStatus status;
    Instant startedAt;
    Instant endedAt;
    String note;
}
