package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public record IATACode(String code) {
    public IATACode {

        Assert.hasText(code, "IATA code must not be blank.");


        Assert.isTrue(code.matches("^[A-Z]{3}$"),
                "IATA code must be exactly 3 uppercase letters.");
    }
}