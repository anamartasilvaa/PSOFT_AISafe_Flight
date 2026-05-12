package pt.isep.psoft.aisafe;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.aisafe.domain.ModelName;

import static org.junit.jupiter.api.Assertions.*;

class ModelNameTest {

    @Test
    void ensureValidModelNameIsCreated() {
        ModelName model = new ModelName("A320neo");
        assertEquals("A320neo", model.name());
    }

    @Test
    void ensureModelNameCannotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ModelName("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new ModelName(null);
        });
    }
}