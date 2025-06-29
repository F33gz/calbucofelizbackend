package cl.metspherical.calbucofelizbackend.features.auth.utils;

/**
 * Simple validator for Chilean phone numbers
 */
public class PhoneValidator {

    // Private constructor to hide the implicit public one
    private PhoneValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validates Chilean phone number (local format only)
     * @param phone The phone number to validate
     * @return true if valid Chilean phone format
     */
    public static boolean isValidChileanPhone(String phone) {
        if (phone == null) return false;
        // Regex for 9-digit numbers starting with 9, optionally prefixed with 09.
        // Ensures the part after the prefix starts with a digit from 2-9, followed by 7 digits.
        return phone.matches("^((09)|9)[2-9]\\d{7}$");
    }
}
