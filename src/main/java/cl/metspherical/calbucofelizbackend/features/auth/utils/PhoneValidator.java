package cl.metspherical.calbucofelizbackend.features.auth.utils;

/**
 * Simple validator for Chilean phone numbers
 */
public class PhoneValidator {

    /**
     * Validates Chilean phone number (local format only)
     * @param phone The phone number to validate
     * @return true if valid Chilean phone format
     */
    public static boolean isValidChileanPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("^(0?9)?[98765432]\\d{7}$");
    }
}
