package marius.server;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for methods in the {@code Tools} class.
 * @author : Marius Berinde Dumutru
 */
public class ToolTests {

    private static final Logger log = LoggerFactory.getLogger(ToolTests .class);


/**
 * Tests the {@code isValidEmail} method to ensure it correctly identifies
 * valid and invalid email formats, including null and empty strings.
 */
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
    /**
     * Tests password hashing and verification using Argon2PasswordEncoder,
     * ensuring the hashed password matches the original plain text.
     */
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
    /**
     * Tests the {@code isValidIp} method with various IPv4 addresses,
     * including boundary cases, private IP ranges, and clearly invalid inputs.
     */
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


/**
 * Tests working with {@code ObjectNode} and {@code ArrayNode} using Jackson,
 * demonstrating how to build JSON objects and extract values as comma-separated strings.
 */
@Test
    void testVector(){
        String username="t1", ip = "193.168.111.111", auditor = "lol";
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode main = mapper.createObjectNode();
        ObjectNode lynis = mapper.createObjectNode();

        main.put("username", username);
        lynis.put("ip", ip);
        lynis.put("auditor", auditor);

        ArrayNode list = mapper.createArrayNode();
        list.add("ACCT-2754");  list.add("ACCT-2760");
        lynis.put("listIdSkippedTest", list);
        main.put("lynis", lynis);
        JsonNode objList =  main.get("lynis").get("listIdSkippedTest");
       String row="";
        if( objList.isArray() ){

           for( int i=0; i<objList.size(); i++ ){
               row += (i<objList.size()-1)? objList.get(i).asText()+","  :objList.get(i).asText();
           }


           System.out.println(row);
        }
        else {
            System.out.println("non array");
        }

    }

    @Test
    /**
     * test used for understand how to manage how to work with DateTimeFormatter
     */
    void testTresformStringToTimeStamp(){

        String stringLogJson ="Mon May 05 2025 10:48:30 GMT+0200 (Ora legale dell’Europa centrale)";
        String cleanedLog = stringLogJson.replaceAll("\\s*\\([^)]*\\)\\s*", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);

        // Parsa e crea il Timestamp
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(cleanedLog, formatter);
        Timestamp logTime = Timestamp.valueOf(offsetDateTime.toLocalDateTime());
        System.out.printf("Stringa json=%s\tTimeStamp= %s\n",stringLogJson,logTime.toString());


    }

}
