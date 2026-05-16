package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public class PartNumber {

    private String number;

    protected PartNumber() {}

    public PartNumber(String number) {
        Assert.hasText(number, "Part number is required.");
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}