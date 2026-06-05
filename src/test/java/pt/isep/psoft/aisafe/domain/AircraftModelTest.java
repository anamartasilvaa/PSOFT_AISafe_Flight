package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    @Test
    void ensureValidAircraftModelIsCreated() {

        ModelName name = new ModelName("737 MAX");

        // Adicionados os dois últimos parâmetros ("3-3 Economy" e null)
        AircraftModel model = new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", "3-3 Economy", null);

        assertNotNull(model);
    }

    @Test
    void ensureAircraftModelCannotHaveZeroOrNegativeSeating() {
        ModelName name = new ModelName("737 MAX");

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, Manufacturer.BOEING, 0, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", null, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, Manufacturer.BOEING, -50, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", null, null);
        });
    }

    @Test
    void ensureAircraftModelCannotHaveInvalidFuelCapacity() {
        ModelName name = new ModelName("737 MAX");

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, Manufacturer.BOEING, 180, 0.0, 6500.0, 839.0, "http://example.com/photo.jpg", null, null);
        });
    }

    @Test
    void ensureAircraftModelCannotBeCreatedWithoutManufacturer() {
        ModelName name = new ModelName("737 MAX");

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(name, null, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", null, null);
        });
    }

    @Test
    void ensureUpdateSpecificationsWorksAndValidates() {
        ModelName name = new ModelName("737 MAX");
        AircraftModel model = new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", "3-3 Economy", null);

        // Atualizar com dados válidos (deve passar)
        assertDoesNotThrow(() -> model.updateSpecifications(200, 26000.0, null, null, "3-3 Config", null));
        assertEquals(200, model.getDefaultSeatingCapacity());
        assertEquals(26000.0, model.getFuelCapacity());
        assertEquals("3-3 Config", model.getSeatingConfiguration());

        // Tentar atualizar com valores negativos (deve falhar e manter o NFR de Validação)
        assertThrows(IllegalArgumentException.class, () -> model.updateSpecifications(-10, null, null, null, null, null));
    }

    @Test
    void ensureUpdateImageWorksCorrectly() {
        ModelName name = new ModelName("737 MAX");
        AircraftModel model = new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/old.jpg", "3-3", null);

        model.updateImage("http://example.com/new_map.png");

        assertEquals("http://example.com/new_map.png", model.getModelPhotoUrl());
    }
}