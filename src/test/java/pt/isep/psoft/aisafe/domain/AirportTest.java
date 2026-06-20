package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AirportTest {

    private Coordinates createValidCoordinates() {
        return new Coordinates(41.2356, -8.6781);
    }

    @Test
    void ensureValidAirportIsCreated() {
        IATACode iata = new IATACode("OPO");
        Airport airport = new Airport(iata, "Francisco Sá Carneiro", "Porto", "Portugal", "GMT+1", AirportType.INTERNATIONAL, createValidCoordinates());
        assertNotNull(airport);
        assertEquals("Porto", airport.getCity());
    }

    @Test
    void ensureCanAddRunwayToAirport() {
        Airport airport = new Airport(new IATACode("OPO"), "Sá Carneiro", "Porto", "Portugal", "GMT+1", AirportType.INTERNATIONAL, createValidCoordinates());
        Runway runway = new Runway("17/35", 3480.0, "North-South");
        assertDoesNotThrow(() -> airport.addRunway(runway));
    }

    @Test
    void ensureCanAddCertificationToAirport() {
        Airport airport = new Airport(new IATACode("OPO"), "Sá Carneiro", "Porto", "Portugal", "GMT+1", AirportType.INTERNATIONAL, createValidCoordinates());

        AircraftModel model = new AircraftModel(
                new ModelName("A320"), Manufacturer.AIRBUS, 180, 20000.0, 5000.0, 800.0,
                500.0, "http://example.com/photo.jpg", "3-3 Economy", "24/7", "Turbofan"
        );

        AirplaneCertification cert = new AirplaneCertification("CERT-123", model, LocalDate.now(), LocalDate.now().plusYears(5));

        assertDoesNotThrow(() -> airport.addOrUpdateAirplaneCertification(cert));
        assertThrows(IllegalArgumentException.class, () -> airport.addOrUpdateAirplaneCertification(null));
    }
}