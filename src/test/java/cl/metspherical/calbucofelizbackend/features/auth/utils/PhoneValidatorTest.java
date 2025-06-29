package cl.metspherical.calbucofelizbackend.features.auth.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneValidatorTest {

    @Test
    void testValidChileanPhoneNumber() {
        assertTrue(PhoneValidator.isValidChileanPhone("987654321"));
    }

    @Test
    void testValidChileanPhoneNumberWithLeadingZero() {
        assertTrue(PhoneValidator.isValidChileanPhone("0987654321"));
    }

    @Test
    void testInvalidChileanPhoneNumberTooShort() {
        assertFalse(PhoneValidator.isValidChileanPhone("98765432"));
    }

    @Test
    void testInvalidChileanPhoneNumberTooLong() {
        assertFalse(PhoneValidator.isValidChileanPhone("9876543210"));
    }

    @Test
    void testInvalidChileanPhoneNumberWithLetters() {
        assertFalse(PhoneValidator.isValidChileanPhone("98765432A"));
    }

    @Test
    void testInvalidChileanPhoneNumberStartsWithInvalidDigit() {
        assertFalse(PhoneValidator.isValidChileanPhone("187654321"));
    }

    @Test
    void testNullPhoneNumber() {
        assertFalse(PhoneValidator.isValidChileanPhone(null));
    }

    @Test
    void testEmptyPhoneNumber() {
        assertFalse(PhoneValidator.isValidChileanPhone(""));
    }
}
