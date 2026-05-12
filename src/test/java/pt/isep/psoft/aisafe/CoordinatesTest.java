package pt.isep.psoft.aisafe;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.aisafe.domain.Coordinates;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {

    @Test
    void ensureValidCoordinatesAreCreated() {
        Coordinates coords = new Coordinates(41.2356, -8.6781); // Francisco Sá Carneiro Airport
        assertEquals(41.2356, coords.latitude());
        assertEquals(-8.6781, coords.longitude());
    }

    @Test
    void ensureCoordinatesCannotBeOutOBounds() {
        // Invalid Latitude
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(91.0, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(-91.0, 0.0));

        // Invalid Longitude
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(0.0, 181.0));
        assertThrows(IllegalArgumentException.class, () -> new Coordinates(0.0, -181.0));
    }
}