package pt.isep.psoft.aisafe;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.aisafe.domain.Airport;
import pt.isep.psoft.aisafe.domain.AirportType;
import pt.isep.psoft.aisafe.domain.IATACode;

import static org.junit.jupiter.api.Assertions.*;

class AirportTest {

    @Test
    void ensureValidAirportIsCreated() {
        // Arrange
        IATACode iata = new IATACode("LIS");
        String name = "Humberto Delgado";
        AirportType type = AirportType.INTERNATIONAL;

        // Act
        Airport airport = new Airport(iata, name, type);

        // Assert
        assertNotNull(airport);
    }

    @Test
    void ensureAirportCannotBeCreatedWithoutIATACode() {
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(null, "Humberto Delgado", AirportType.INTERNATIONAL);
        });
    }

    @Test
    void ensureAirportCannotBeCreatedWithoutName() {
        IATACode iata = new IATACode("LIS");

        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(iata, "", AirportType.INTERNATIONAL);
        });
    }
}