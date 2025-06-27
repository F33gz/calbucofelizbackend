package cl.metspherical.calbucofelizbackend.features.auth.utils;

import java.util.stream.IntStream;

/**
 * Utility class for validating Chilean RUT (Rol Único Tributario)
 * Implements the standard Chilean RUT validation algorithm with check digit calculation
 */
public class RutValidator {

    /**
     * Validates a Chilean RUT using the official check digit algorithm with functional programming
     * @param rut The RUT string to validate (e.g., "12345678-9", "12.345.678-K")
     * @return true if the RUT is valid, false otherwise
     */
    public static boolean validateRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Clean and normalize the RUT in one line
            String cleanRut = rut.toUpperCase().replaceAll("[.-]", "");
            
            // Validate minimum length and format
            if (cleanRut.length() < 2 || !cleanRut.matches("\\d+[0-9K]")) {
                return false;
            }
            
            // Extract parts
            String numberPart = cleanRut.substring(0, cleanRut.length() - 1);
            char checkDigit = cleanRut.charAt(cleanRut.length() - 1);
            
            // Calculate check digit using functional approach
            int sum = IntStream.range(0, numberPart.length())
                    .map(i -> {
                        int digit = Character.getNumericValue(numberPart.charAt(numberPart.length() - 1 - i));
                        int multiplier = 2 + (i % 6);
                        return digit * multiplier;
                    })
                    .sum();
            
            int remainder = 11 - (sum % 11);
            char calculatedDigit = (remainder == 11) ? '0' : (remainder == 10) ? 'K' : (char) ('0' + remainder);
            
            return checkDigit == calculatedDigit;

        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return false;
        }
    }
}
