package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IATACodeTest {

    @Test
    void ensureValidIATACodeIsCreated() {

        IATACode iata = new IATACode("OPO");


        assertEquals("OPO", iata.code());
    }

    @Test
    void ensureIATACodeCannotBeBlank() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IATACode("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new IATACode(null);
        });
    }

    @Test
    void ensureIATACodeMustBeExactlyThreeUppercaseLetters() {
        // Letras a mais
        assertThrows(IllegalArgumentException.class, () -> {
            new IATACode("OPOO");
        });

        // Letras minúsculas
        assertThrows(IllegalArgumentException.class, () -> {
            new IATACode("opo");
        });

        // Números
        assertThrows(IllegalArgumentException.class, () -> {
            new IATACode("123");
        });
    }
}