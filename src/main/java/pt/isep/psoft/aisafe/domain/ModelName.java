package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public record ModelName(String name) {
    public ModelName {
        Assert.hasText(name, "The model name cannot be empty.");
    }

    @Override
    public String toString() {
        return this.name;
    }
}