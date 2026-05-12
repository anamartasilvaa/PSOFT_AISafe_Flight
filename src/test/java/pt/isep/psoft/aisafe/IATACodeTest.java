package pt.isep.psoft.aisafe;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.aisafe.domain.IATACode;

import static org.junit.jupiter.api.Assertions.*;

class IATACodeTest {

    @Test
    void ensureValidIATACodeIsCreated() {
        // Arrange & Act
        IATACode iata = new IATACode("OPO");

        // Assert
        assertEquals("OPO", iata.code());
    }

    @Test
    void ensureIATACodeCannotBeBlank() {
        // Assert que lança uma IllegalArgumentException (vinda do Assert.hasText)
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