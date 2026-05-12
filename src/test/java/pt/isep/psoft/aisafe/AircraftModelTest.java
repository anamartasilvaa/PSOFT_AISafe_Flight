package pt.isep.psoft.aisafe;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.aisafe.domain.AircraftModel;
import pt.isep.psoft.aisafe.domain.Manufacturer;
import pt.isep.psoft.aisafe.domain.ModelName;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    @Test
    void ensureValidAircraftModelIsCreated() {
        // Arrange
        ModelName name = new ModelName("737 MAX");

        // Act - Adicionado o 7º argumento
        AircraftModel model = new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg");

        // Assert
        assertNotNull(model);
    }

    @Test
    void ensureAircraftModelCannotHaveZeroOrNegativeSeating() {
        ModelName name = new ModelName("737 MAX");

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, Manufacturer.BOEING, 0, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, Manufacturer.BOEING, -50, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg");
        });
    }

    @Test
    void ensureAircraftModelCannotHaveInvalidFuelCapacity() {
        ModelName name = new ModelName("737 MAX");

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, Manufacturer.BOEING, 180, 0.0, 6500.0, 839.0, "http://example.com/photo.jpg");
        });
    }

    @Test
    void ensureAircraftModelCannotBeCreatedWithoutManufacturer() {
        ModelName name = new ModelName("737 MAX");

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, null, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg");
        });
    }
}