package cl.metspherical.calbucofelizbackend.features.auth.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RutValidatorTest {

    @Test
    void testValidRutWithHyphen() {
        assertTrue(RutValidator.validateRut("12345678-5"));
    }

    @Test
    void testValidRutWithDotsAndHyphen() {
        assertTrue(RutValidator.validateRut("12.345.678-5"));
    }

    @Test
    void testValidRutWithoutFormatting() {
        assertTrue(RutValidator.validateRut("123456785"));
    }

    @Test
    void testValidRutWithK() {
        assertTrue(RutValidator.validateRut("5720690-K")); // Using a commonly cited valid K RUT
    }

    @Test
    void testInvalidRutIncorrectCheckDigit() {
        assertFalse(RutValidator.validateRut("12345678-0"));
    }

    @Test
    void testInvalidRutIncorrectFormat() {
        assertFalse(RutValidator.validateRut("123456789-0")); // Too many numbers
    }

    @Test
    void testInvalidRutShort() {
        assertFalse(RutValidator.validateRut("1-0"));
    }

    @Test
    void testInvalidRutLetterInNumberPart() {
        assertFalse(RutValidator.validateRut("1234567A-5"));
    }

    @Test
    void testNullRut() {
        assertFalse(RutValidator.validateRut(null));
    }

    @Test
    void testEmptyRut() {
        assertFalse(RutValidator.validateRut(""));
    }

    @Test
    void testBlankRut() {
        assertFalse(RutValidator.validateRut("   "));
    }
}
