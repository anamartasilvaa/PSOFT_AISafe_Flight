package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AirplaneCertificationTest {

    private AircraftModel createDummyModel() {
        // Adicionado o 7º argumento
        return new AircraftModel(new ModelName("A320"), Manufacturer.AIRBUS, 180, 20000.0, 5000.0, 800.0, "http://example.com/photo.jpg");
    }

    @Test
    void ensureValidCertificationIsCreated() {
        AircraftModel model = createDummyModel();
        LocalDate issue = LocalDate.of(2023, 1, 1);
        LocalDate expiry = LocalDate.of(2028, 1, 1);

        AirplaneCertification cert = new AirplaneCertification("CERT-123", model, issue, expiry);
        assertNotNull(cert);
    }

    @Test
    void ensureExpiryDateMustBeAfterIssueDate() {
        AircraftModel model = createDummyModel();
        LocalDate issue = LocalDate.of(2023, 1, 1);
        LocalDate expiry = LocalDate.of(2022, 1, 1); // Expiry before issue

        assertThrows(IllegalArgumentException.class, () -> {
            new AirplaneCertification("CERT-123", model, issue, expiry);
        });
    }
}