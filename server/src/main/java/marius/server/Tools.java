package marius.server;

import marius.server.controller.AuthController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.util.regex.Pattern;

public class Tools {

    private static final Logger log = LoggerFactory.getLogger(Tools.class);
    private static int saltLength=16,  hashLength=32, parallelism=4, memory=65536 , iterations=8;
    private static final Argon2PasswordEncoder encoder =new Argon2PasswordEncoder(saltLength,  hashLength, parallelism, memory , iterations);

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

    /**
     * Hashes a password using the Argon2id algorithm with predefined security parameters.
     *
     * <p>This method uses the following Argon2 configuration:
     * <ul>
     *   <li>Salt length: 16 bytes (randomly generated)</li>
     *   <li>Hash length: 16 bytes</li>
     *   <li>Parallelism: 4 threads</li>
     *   <li>Memory: 47104 KB (~46 MB)</li>
     *   <li>Iterations: 8</li>
     * </ul>
     *
     * @param password the plain text password to hash (must not be null or empty)
     * @return a securely hashed password string including salt and parameters
     * @throws IllegalArgumentException if either parameter is null
     */
    public static String hashPassword(String password){
        if (password == null){
            throw new IllegalArgumentException("Password and hash cannot be null");
        }
        return encoder.encode(password);
    }

    /**
     * Verifies if a plain text password matches the given Argon2 hash.
     *
     * @param plain the plain text password to verify
     * @param encoded the Argon2 hash to check against
     * @return true if the plain password matches the hash, false otherwise
     * @throws IllegalArgumentException if either parameter is null
     */
    public static boolean isPasswordHashedWith(String plain, String encoded){
        if (plain == null || encoded == null) {
            throw new IllegalArgumentException("Password and hash cannot be null");
        }
        return encoder.matches(plain, encoded);
    }

    /**
     * Check if IP is a valid IP address
     * @param ip address
     * @return true if is a valid IP address , false otherwise
     */
    public static boolean isValidIp(String ip){
        if (ip == null){
            return false;
        }
        String local=ip.trim();
        int minLen=6,maxLen=15;
       if(local.length()<minLen || local.length()>maxLen){
           return false;
       }
       String nrs[] = local.split("\\.");
       if(nrs.length!=4){
           return false;
       }
       for(int i=0;i<nrs.length;i++){
           try {
               int nr=Integer.parseInt(nrs[i]);
               if(nr<0 || nr>255){
                   return false;
               }
           }catch (NumberFormatException e){
              return false;
           }
       }
       return true;

    }
}
