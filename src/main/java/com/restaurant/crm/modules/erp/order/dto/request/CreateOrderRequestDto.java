package com.restaurant.crm.modules.erp.order.dto.request;

import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderRequestDto {

    @NotBlank
    String branchId;

    String tableId;

    String reservationId;

    @NotNull
    OrderType orderType;

    @Size(max = OrderConstants.MAX_CHARS_CUSTOMER_NAME)
    String customerName;

    @Size(max = OrderConstants.MAX_CHARS_CUSTOMER_PHONE)
    String customerPhone;

    @Size(max = OrderConstants.MAX_CHARS_NOTE)
    String note;

    @NotEmpty
    @Valid
    List<CreateOrderItemRequestDto> items;
}
