package com.restaurant.crm.modules.erp.order.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restaurant.crm.modules.crm.customeraccount.model.OtpTicketPayload;
import com.restaurant.crm.modules.crm.customeraccount.service.interfaces.OtpTicketService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpTicketVerifierImplTest {

    private static final String PHONE = "0987654321";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final String TICKET = "otp-ticket";

    @Mock OtpTicketService otpTicketService;
    @InjectMocks OtpTicketVerifierImpl verifier;

    @Test
    void validWhenPayloadMatchesPhoneBranchTable() {
        when(otpTicketService.verify(TICKET))
                .thenReturn(Optional.of(new OtpTicketPayload(PHONE, BRANCH, TABLE)));

        // Caller passes a non-normalized phone; the adapter normalizes before comparing.
        assertTrue(verifier.isValid("+84987654321", BRANCH, TABLE, TICKET));
    }

    @Test
    void invalidWhenTicketStructurallyInvalid() {
        when(otpTicketService.verify(TICKET)).thenReturn(Optional.empty());
        assertFalse(verifier.isValid(PHONE, BRANCH, TABLE, TICKET));
    }

    @Test
    void invalidWhenPhoneDiffers() {
        when(otpTicketService.verify(TICKET))
                .thenReturn(Optional.of(new OtpTicketPayload(PHONE, BRANCH, TABLE)));
        assertFalse(verifier.isValid("0912345678", BRANCH, TABLE, TICKET));
    }

    @Test
    void invalidWhenBranchDiffers() {
        when(otpTicketService.verify(TICKET))
                .thenReturn(Optional.of(new OtpTicketPayload(PHONE, BRANCH, TABLE)));
        assertFalse(verifier.isValid(PHONE, "branch-2", TABLE, TICKET));
    }

    @Test
    void invalidWhenTableDiffers() {
        when(otpTicketService.verify(TICKET))
                .thenReturn(Optional.of(new OtpTicketPayload(PHONE, BRANCH, TABLE)));
        assertFalse(verifier.isValid(PHONE, BRANCH, "table-2", TICKET));
    }
}
