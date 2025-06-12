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

    @Test
    void testValidIp(){
        String minLenght="0.0.0.0";
        String maxLenght="255.255.255.255";
        String avg="109.198.168.1";
        String avg1="1.1.1.1";
        String avg2= "10.0.0.1";        // IP privato classe A
        String avg3= "172.16.254.1";    // IP privato classe B
        String avg4= "8.8.8.8";
        String avg5= "193.168.111.111";

        assertTrue(Tools.isValidIp(minLenght),"minLenght valid");
        assertTrue(Tools.isValidIp(maxLenght),"maxLenght valid");
        assertTrue(Tools.isValidIp(avg),"avg valid");
        assertTrue(Tools.isValidIp(avg1),"avg forse problematico valid");
        assertTrue(Tools.isValidIp(avg2),"avg forse problematico valid");
        assertTrue(Tools.isValidIp(avg3),"avg forse problematico valid");
        assertTrue(Tools.isValidIp(avg4),"avg forse problematico valid");
        assertTrue(Tools.isValidIp(avg5),"avg forse problematico valid");

        String e1="";
        String e2="255.255.255.255.255";
        String e3="...";
        String e4="A.B.C.E";
        String e5="256.0.30.111";
        String e6="11.3.2.-23";
        String e7="1.1.1";
        assertFalse(Tools.isValidIp(e1),"empty string ");
        assertFalse(Tools.isValidIp(e2),"too long  error ");
        assertFalse(Tools.isValidIp(e3),"only dots error ");
        assertFalse(Tools.isValidIp(e4),"not numbers error ");
        assertFalse(Tools.isValidIp(e5),"out of range max error ");
        assertFalse(Tools.isValidIp(e6),"out of range min error ");
        assertFalse(Tools.isValidIp(e7)," max error ");

    }

}
