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

        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        LocalDate manufacturingDate = LocalDate.of(2022, 5, 10);


        Aircraft aircraft = new Aircraft(regNum, model, manufacturingDate, 180);


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


        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(null, model, manufacturingDate, 180);
        });


        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(regNum, null, manufacturingDate, 180);
        });
    }

    @Test
    void ensureUpdateStatusWorksCorrectly() {

        RegistrationNumber regNum = new RegistrationNumber("CS-TPA");
        AircraftModel model = createValidModel();
        Aircraft aircraft = new Aircraft(regNum, model, LocalDate.now(), 180);


        assertDoesNotThrow(() -> aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE));


        assertThrows(IllegalArgumentException.class, () -> aircraft.updateStatus(null));
    }
}