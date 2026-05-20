package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunwayTest {

    @Test
    void ensureValidRunwayIsCreated() {
        Runway runway = new Runway("17/35", 3480.0, "North-South");
        assertNotNull(runway);
    }

    @Test
    void ensureRunwayCannotHaveZeroOrNegativeLength() {
        assertThrows(IllegalArgumentException.class, () -> new Runway("17/35", 0.0, "North-South"));
        assertThrows(IllegalArgumentException.class, () -> new Runway("17/35", -100.0, "North-South"));
    }

    @Test
    void ensureRunwayCannotHaveBlankNameOrOrientation() {
        assertThrows(IllegalArgumentException.class, () -> new Runway("", 3480.0, "North-South"));
        assertThrows(IllegalArgumentException.class, () -> new Runway("17/35", 3480.0, ""));
    }
}