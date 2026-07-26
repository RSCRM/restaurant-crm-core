package com.restaurant.crm.modules.crm.customer_account.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNumberUtilsTest {

    @Test
    void normalizeMapsAllPrefixesToCanonicalForm() {
        assertEquals("0987654321", PhoneNumberUtils.normalize("+84987654321"));
        assertEquals("0987654321", PhoneNumberUtils.normalize("84987654321"));
        assertEquals("0987654321", PhoneNumberUtils.normalize("0987654321"));
        assertEquals("0987654321", PhoneNumberUtils.normalize("098 765 4321"));
        assertEquals("0987654321", PhoneNumberUtils.normalize("098-765.4321"));
    }

    @Test
    void normalizeReturnsNullForNull() {
        assertNull(PhoneNumberUtils.normalize(null));
    }

    @Test
    void allEquivalentInputsNormalizeToSameKey() {
        String canonical = PhoneNumberUtils.normalize("0987654321");
        assertEquals(canonical, PhoneNumberUtils.normalize("+84987654321"));
        assertEquals(canonical, PhoneNumberUtils.normalize("84987654321"));
        assertEquals(canonical, PhoneNumberUtils.normalize("098 765 4321"));
    }

    @Test
    void isValidAcceptsVietnameseMobileNumbers() {
        assertTrue(PhoneNumberUtils.isValid("0987654321"));
        assertTrue(PhoneNumberUtils.isValid("0321234567"));
        assertTrue(PhoneNumberUtils.isValid("0523456789"));
    }

    @Test
    void isValidRejectsMalformedNumbers() {
        assertFalse(PhoneNumberUtils.isValid("0123456789")); // second digit 1 not allowed
        assertFalse(PhoneNumberUtils.isValid("abc"));
        assertFalse(PhoneNumberUtils.isValid(""));
        assertFalse(PhoneNumberUtils.isValid(null));
        assertFalse(PhoneNumberUtils.isValid("098765432"));   // too short
        assertFalse(PhoneNumberUtils.isValid("09876543210")); // too long
    }

    @Test
    void maskHidesMiddleDigits() {
        assertEquals("0987***321", PhoneNumberUtils.mask("0987654321"));
        assertEquals("***", PhoneNumberUtils.mask(null));
        assertEquals("***", PhoneNumberUtils.mask("12345"));
    }
}
