package com.restaurant.crm.modules.erp.order.dto.response;

import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupCartResponse {

    String sessionId;
    String tableId;
    SessionMemberRole role;

    @Builder.Default
    List<GroupCartItemResponse> items = new ArrayList<>();

    BigDecimal subtotal;
    Integer itemCount;
    boolean submitAllowed;
}
