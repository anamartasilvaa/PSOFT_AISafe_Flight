package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public record Coordinates(Double latitude, Double longitude) {

    public Coordinates {
        Assert.notNull(latitude, "Latitude must not be null.");
        Assert.notNull(longitude, "Longitude must not be null.");
        Assert.isTrue(latitude >= -90.0 && latitude <= 90.0,
                "Latitude must be between -90 and 90 degrees.");
        Assert.isTrue(longitude >= -180.0 && longitude <= 180.0,
                "Longitude must be between -180 and 180 degrees.");
    }
}