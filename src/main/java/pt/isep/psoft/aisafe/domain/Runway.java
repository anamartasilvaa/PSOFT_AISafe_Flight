package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;

@Entity
public class Runway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double length;

    @Column(nullable = false)
    private String orientation;

    protected Runway() {}

    public Runway(String name, Double length, String orientation) {
        Assert.hasText(name, "Runway name must not be blank.");
        Assert.notNull(length, "Runway length must not be null.");
        Assert.isTrue(length > 0, "Runway length must be greater than zero.");
        Assert.hasText(orientation, "Runway orientation must not be blank.");

        this.name = name;
        this.length = length;
        this.orientation = orientation;
    }


    public String getName() {
        return name;
    }

    public Double getLength() {
        return length;
    }

    public String getOrientation() {
        return orientation;
    }
}