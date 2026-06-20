package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AirplaneCertificationTest {

    private AircraftModel createDummyModel() {
        ModelName name = new ModelName("B737 MAX");
        return new AircraftModel(name, Manufacturer.BOEING, 180, 25900.0, 6500.0, 839.0, "http://example.com/photo.jpg", "3-3 Economy", null, "Turbofan");
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
        LocalDate expiry = LocalDate.of(2022, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> new AirplaneCertification("CERT-123", model, issue, expiry));
    }
}