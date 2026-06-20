package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    @Test
    void ensureValidAircraftModelIsCreated() {
        ModelName name = new ModelName("737 MAX");
        // Construtor com 10 parâmetros (compatibilidade)
        AircraftModel model = new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", "3-3 Economy", null, "Turbofan");

        assertNotNull(model);
        assertEquals("Turbofan", model.getEngineType());
    }

    @Test
    void ensureAircraftModelCannotHaveZeroOrNegativeSeating() {
        ModelName name = new ModelName("737 MAX");
        // O construtor desta classe valida o seatingCapacity > 0
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(name, Manufacturer.BOEING, 0, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", null, null, "Turbofan"));
    }

    @Test
    void ensureAircraftModelCannotHaveInvalidFuelCapacity() {
        ModelName name = new ModelName("737 MAX");
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(name, Manufacturer.BOEING, 180, 0.0, 6500.0, 839.0, "http://example.com/photo.jpg", null, null, "Turbofan"));
    }

    @Test
    void ensureAircraftModelCannotBeCreatedWithoutManufacturer() {
        ModelName name = new ModelName("737 MAX");
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(name, null, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", null, null, "Turbofan"));
    }

    @Test
    void ensureUpdateSpecificationsWorksAndValidates() {
        ModelName name = new ModelName("737 MAX");
        AircraftModel model = new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", "3-3 Economy", null, "Turbofan");

        // Atualizar com dados válidos
        assertDoesNotThrow(() -> model.updateSpecifications(200, 26000.0, 7000.0, 850.0, "3-3 Config", "24/7"));
        assertEquals(200, model.getDefaultSeatingCapacity());
        assertEquals(26000.0, model.getFuelCapacity());
        assertEquals("3-3 Config", model.getSeatingConfiguration());

        // Tentar atualizar com valores negativos (deve falhar)
        assertThrows(IllegalArgumentException.class, () -> model.updateSpecifications(-10, null, null, null, null, null));
    }

    @Test
    void ensureUpdateImageWorksCorrectly() {
        ModelName name = new ModelName("737 MAX");
        AircraftModel model = new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/old.jpg", "3-3", null, "Turbofan");

        model.updateImage("http://example.com/new_map.png");
        assertEquals("http://example.com/new_map.png", model.getModelPhotoUrl());
    }
}