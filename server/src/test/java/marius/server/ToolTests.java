package marius.server;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolTests {

    private static final Logger log = LoggerFactory.getLogger(ToolTests .class);

    @Test
    void checkisValidEmail(){
        String[] validFormats = {
                "user@example.com",
                "test.email@domain.org",
                "user123@test-domain.co.uk",
                "name+tag@example.net",
                "simple_name@domain.it"
        };

        // Array di email non valide
        String[] invalidFormats = {
                "plaintext",
                "@domain.com",
                "user@",
                "user@domain",
                "user@domain.c"
        };

        // Test per tutti i formati validi
        for (String email : validFormats) {
            assertTrue(Tools.isValidEmail(email),
                    "L'email dovrebbe essere valida: " + email);
        }

        // Test per tutti i formati non validi
        for (String email : invalidFormats) {
            assertFalse(Tools.isValidEmail(email),
                    "L'email non dovrebbe essere valida: " + email);
        }

        // Test per casi speciali
        assertFalse(Tools.isValidEmail(null),
                "null non dovrebbe essere valida");

        assertFalse(Tools.isValidEmail(""),
                "stringa vuota non dovrebbe essere valida");
    }

    @Test
    void testEncodeWithArgon(){
        String plain1 = "Sudo";
        String plain2 = "abcd";
        String plain3 = "abcd";
        String hash1 = Tools.hashPassword(plain1);
        String hash2 = Tools.hashPassword(plain2);
        String hash3 = Tools.hashPassword(plain3);
        log.info("hash1: " + hash1);
        log.info("hash2: " + hash2);

        assertTrue(Tools.isPasswordHashedWith(plain1, hash1),"Test1:same object correspond to hash");
        assertTrue(Tools.isPasswordHashedWith(plain2, hash3),"Test2:same text match to hash");
        assertFalse(Tools.isPasswordHashedWith(plain1, hash2),"Test2:different text not  match to hash");
    }

}
