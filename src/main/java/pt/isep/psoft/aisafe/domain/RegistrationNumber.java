package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;
@Embeddable
public record RegistrationNumber(String number) {

    public RegistrationNumber {

        Assert.hasText(number, "Registration number must not be blank.");


        Assert.isTrue(number.matches("^[A-Z0-9]{1,5}-[A-Z0-9]{1,5}$"),
                "Registration number must follow a valid format (e.g., CS-TPA).");
    }

    @Override
    public String toString() {
        return this.number;
    }
}