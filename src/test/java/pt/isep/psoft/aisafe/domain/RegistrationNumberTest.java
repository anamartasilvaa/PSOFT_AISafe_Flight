package pt.isep.psoft.aisafe.domain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationNumberTest {

    @Test
    void ensureValidRegistrationNumberIsCreated() {
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        assertEquals("CS-TPA", regNum.number());
    }

    @Test
    void ensureRegistrationNumberCannotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber(""));
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber(null));
    }

    @Test
    void ensureRegistrationNumberFollowsValidFormat() {
        // Must contain alphanumeric characters and a hyphen
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber("INVALID@NUMBER"));
        assertThrows(IllegalArgumentException.class, () -> new RegistrationNumber("CSTPA"));
    }
}