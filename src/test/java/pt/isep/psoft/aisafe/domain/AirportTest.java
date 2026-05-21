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
        // CORREÇÃO: Adicionados City, Country e Timezone no construtor
        Airport airport = new Airport(iata, "Francisco Sá Carneiro", "Porto", "Portugal", "GMT+1", AirportType.INTERNATIONAL, createValidCoordinates());

        assertNotNull(airport);
        assertEquals("Porto", airport.getCity());
        assertEquals("Portugal", airport.getCountry());
    }

    @Test
    void ensureAirportCannotBeCreatedWithoutCoordinates() {
        IATACode iata = new IATACode("OPO");
        assertThrows(IllegalArgumentException.class, () -> {
            // CORREÇÃO: Adicionados os novos campos aqui também para o teste compilar
            new Airport(iata, "Francisco Sá Carneiro", "Porto", "Portugal", "GMT+1", AirportType.INTERNATIONAL, null);
        });
    }

    @Test
    void ensureCanAddRunwayToAirport() {
        Airport airport = new Airport(new IATACode("OPO"), "Sá Carneiro", "Porto", "Portugal", "GMT+1", AirportType.INTERNATIONAL, createValidCoordinates());
        Runway runway = new Runway("17/35", 3480.0, "North-South");

        assertDoesNotThrow(() -> airport.addRunway(runway));
        // Verifica se a lista de pistas não está vazia (opcional mas bom)
        assertFalse(airport.getRunways().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> airport.addRunway(null));
    }

    @Test
    void ensureCanAddCertificationToAirport() {
        Airport airport = new Airport(new IATACode("OPO"), "Sá Carneiro", "Porto", "Portugal", "GMT+1", AirportType.INTERNATIONAL, createValidCoordinates());

        AircraftModel model = new AircraftModel(new ModelName("A320"), Manufacturer.AIRBUS, 180, 20000.0, 5000.0, 800.0, "http://example.com/photo.jpg");
        AirplaneCertification cert = new AirplaneCertification("CERT-123", model, LocalDate.now(), LocalDate.now().plusYears(5));

        assertDoesNotThrow(() -> airport.addAirplaneCertification(cert));
        assertThrows(IllegalArgumentException.class, () -> airport.addAirplaneCertification(null));
    }

    @Test
    void ensureAirportCannotBeCreatedWithMissingData() {
        IATACode iata = new IATACode("OPO");
        // Teste para garantir que campos obrigatórios (como cidade) não podem ser nulos/vazios
        assertThrows(IllegalArgumentException.class, () -> {
            new Airport(iata, "Sá Carneiro", null, "Portugal", "GMT+1", AirportType.INTERNATIONAL, createValidCoordinates());
        });
    }
}