package marius.server;

import marius.server.controller.AuthController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class Tools {

    private static final Logger log = LoggerFactory.getLogger(Tools.class);

    /**
     * Validates if the provided email address has a valid format according to RFC standards.
     *
     * This method uses regex pattern matching to verify the email structure including:
     * - Valid characters in local part (before @)
     * - Proper domain structure
     * - Valid top-level domain (2-7 characters)
     *
     * @param email the email address to validate (can be null)
     * @return true if the email has a valid format, false otherwise (including null input)
     *
     * @throws PatternSyntaxException if the regex pattern is malformed (should not happen with current implementation)
     *
     * @example
     * <pre>
     * isValidEmail("test@example.com") → true
     * isValidEmail("invalid-email") → false
     * isValidEmail(null) → false
     * </pre>
     *
     * @since 1.0
     */
    public static boolean isValidEmail(String email){
        if(email == null){
            return false;
        }
        String regexPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        log.info("email: " + email+"\t result regex+"+Pattern.matches(regexPattern, email));
        return Pattern.matches(regexPattern, email);
    }
}
