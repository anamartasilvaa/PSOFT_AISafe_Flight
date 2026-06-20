package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    private AircraftModel createValidModel() {
        ModelName modelName = new ModelName("A320neo");
        return new AircraftModel(modelName, Manufacturer.AIRBUS, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", "3-3 Economy", null, "Turbofan");
    }

    @Test
    void ensureValidAircraftIsCreated() {
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        LocalDate manufacturingDate = LocalDate.of(2022, 5, 10);

        Aircraft aircraft = new Aircraft(regNum, model, manufacturingDate, 180, "WiFi, Power Outlets");
        assertNotNull(aircraft);
        assertEquals("WiFi, Power Outlets", aircraft.getFeatures());
    }

    @Test
    void ensureAircraftCannotHaveZeroOrNegativeSeatingCapacity() {
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        LocalDate manufacturingDate = LocalDate.of(2022, 5, 10);

        assertThrows(IllegalArgumentException.class, () -> new Aircraft(regNum, model, manufacturingDate, 0, "None"));
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(regNum, model, manufacturingDate, -10, "None"));
    }

    @Test
    void ensureAircraftCannotBeCreatedWithNullValues() {
        AircraftModel model = createValidModel();
        LocalDate manufacturingDate = LocalDate.of(2022, 5, 10);

        assertThrows(IllegalArgumentException.class, () -> new Aircraft(null, model, manufacturingDate, 180, "None"));
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        assertThrows(IllegalArgumentException.class, () -> new Aircraft(regNum, null, manufacturingDate, 180, "None"));
    }

    @Test
    void ensureUpdateStatusWorksCorrectly() {
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        Aircraft aircraft = new Aircraft(regNum, model, LocalDate.now(), 180, "None");

        assertDoesNotThrow(() -> aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE));
        assertThrows(IllegalArgumentException.class, () -> aircraft.updateStatus(null));
    }
}