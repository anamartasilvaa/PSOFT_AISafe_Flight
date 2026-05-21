package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    private AircraftModel createValidModel() {
        ModelName modelName = new ModelName("A320neo");
        return new AircraftModel(
                modelName,
                Manufacturer.AIRBUS,
                180,
                25000.0,
                6000.0,
                830.0,
                "http://example.com/photo.jpg"
        );
    }

    @Test
    void ensureValidAircraftIsCreated() {
        // Arrange
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        LocalDate manufacturingDate = LocalDate.of(2022, 5, 10);

        // Act
        Aircraft aircraft = new Aircraft(regNum, model, manufacturingDate, 180);

        // Assert
        assertNotNull(aircraft);
    }

    @Test
    void ensureAircraftCannotHaveZeroOrNegativeSeatingCapacity() {
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        LocalDate manufacturingDate = LocalDate.of(2022, 5, 10);

        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(regNum, model, manufacturingDate, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(regNum, model, manufacturingDate, -10);
        });
    }

    @Test
    void ensureAircraftCannotBeCreatedWithNullValues() {
        AircraftModel model = createValidModel();
        LocalDate manufacturingDate = LocalDate.of(2022, 5, 10);

        // Null Registration Number
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(null, model, manufacturingDate, 180);
        });

        // Null Model
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(regNum, null, manufacturingDate, 180);
        });
    }

    @Test
    void ensureUpdateStatusWorksCorrectly() {
        // Arrange
        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        Aircraft aircraft = new Aircraft(regNum, model, LocalDate.now(), 180);

        // Act & Assert (No exceptions should be thrown)
        assertDoesNotThrow(() -> aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE));

        // Null status should throw exception
        assertThrows(IllegalArgumentException.class, () -> aircraft.updateStatus(null));
    }
}