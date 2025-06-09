package marius.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolTests {

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

}
