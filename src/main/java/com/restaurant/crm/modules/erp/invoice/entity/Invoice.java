package com.restaurant.crm.modules.erp.invoice.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.invoice.constants.InvoiceConstants;
import com.restaurant.crm.modules.erp.invoice.enums.InvoiceStatus;
import com.restaurant.crm.modules.erp.invoice.enums.PaymentMethod;
import com.restaurant.crm.modules.erp.order.entity.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = InvoiceConstants.TABLE_INVOICE)
public class Invoice extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = InvoiceConstants.COL_ORDER_ID, nullable = false, unique = true)
    Order order;

    @NotBlank
    @NotNull
    @Size(max = InvoiceConstants.MAX_CHARS_INVOICE_CODE)
    @Column(name = InvoiceConstants.COL_INVOICE_CODE, nullable = false, unique = true, columnDefinition = InvoiceConstants.INVOICE_CODE_DEFINITION)
    String invoiceCode;

    @Builder.Default
    @NotNull
    @Column(name = InvoiceConstants.COL_SUBTOTAL, nullable = false, precision = InvoiceConstants.MONEY_PRECISION, scale = InvoiceConstants.MONEY_SCALE)
    BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(name = InvoiceConstants.COL_DISCOUNT_AMOUNT, nullable = false, precision = InvoiceConstants.MONEY_PRECISION, scale = InvoiceConstants.MONEY_SCALE)
    BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(name = InvoiceConstants.COL_TOTAL_AMOUNT, nullable = false, precision = InvoiceConstants.MONEY_PRECISION, scale = InvoiceConstants.MONEY_SCALE)
    BigDecimal totalAmount = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = InvoiceConstants.COL_PAYMENT_METHOD, nullable = false, columnDefinition = InvoiceConstants.ENUM_DEFINITION)
    PaymentMethod paymentMethod;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = InvoiceConstants.COL_STATUS, nullable = false, columnDefinition = InvoiceConstants.ENUM_DEFINITION)
    InvoiceStatus status = InvoiceStatus.PAID;

    @NotNull
    @Column(name = InvoiceConstants.COL_PAID_AT, nullable = false)
    Instant paidAt;

    @Column(name = InvoiceConstants.COL_REFUNDED_AT)
    Instant refundedAt;

    @Size(max = InvoiceConstants.MAX_CHARS_REFUND_REASON)
    @Column(name = InvoiceConstants.COL_REFUND_REASON, columnDefinition = InvoiceConstants.REFUND_REASON_DEFINITION)
    String refundReason;

    @Size(max = InvoiceConstants.MAX_CHARS_NOTE)
    @Column(name = InvoiceConstants.COL_NOTE, columnDefinition = InvoiceConstants.NOTE_DEFINITION)
    String note;
}
