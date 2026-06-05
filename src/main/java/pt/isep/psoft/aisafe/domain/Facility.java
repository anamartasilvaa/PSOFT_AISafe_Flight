package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public class Facility {
    private String type;
    private String description;

    // Construtor protegido para o JPA
    protected Facility() {}

    public Facility(String type, String description) {
        Assert.hasText(type, "Facility type is required.");
        Assert.hasText(description, "Facility description is required.");
        this.type = type;
        this.description = description;
    }

    public String getType() { return type; }
    public String getDescription() { return description; }
}