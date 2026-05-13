package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.util.Objects;

@Entity
public class AircraftModel {

    // 1. Identidade técnica
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    // 2. Identidade de Domínio
    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "model_name", unique = true, nullable = false))
    private ModelName modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Manufacturer manufacturer;

    @Column(nullable = false)
    private Integer defaultSeatingCapacity;

    @Column(nullable = false)
    private Double fuelCapacity;

    @Column(nullable = false)
    private Double maximumRange;

    @Column(nullable = false)
    private Double cruisingSpeed;

    @Column(nullable = true) // Pode ser opcional
    private String modelPhotoUrl;

    // 3. Construtor JPA (Protegido)
    protected AircraftModel() {}

    // 4. Construtor de Negócio
    public AircraftModel(ModelName modelName, Manufacturer manufacturer, Integer defaultSeatingCapacity,
                         Double fuelCapacity, Double maximumRange, Double cruisingSpeed, String modelPhotoUrl) {

        Assert.notNull(modelName, "The model name is required.");
        Assert.notNull(manufacturer, "The manufacturer is required.");

        // Validações de senso comum aeronáutico
        Assert.isTrue(defaultSeatingCapacity != null && defaultSeatingCapacity > 0, "The seating capacity must be greater than 0.");
        Assert.isTrue(fuelCapacity != null && fuelCapacity > 0, "The fuel capacity must be greater than 0.");
        Assert.isTrue(maximumRange != null && maximumRange > 0, "The maximum range must be greater than 0.");
        Assert.isTrue(cruisingSpeed != null && cruisingSpeed > 0, "The cruising speed must be greater than 0.");
        this.modelPhotoUrl = modelPhotoUrl;

        this.modelName = modelName;
        this.manufacturer = manufacturer;
        this.defaultSeatingCapacity = defaultSeatingCapacity;
        this.fuelCapacity = fuelCapacity;
        this.maximumRange = maximumRange;
        this.cruisingSpeed = cruisingSpeed;
    }

    // 5. equals e hashCode baseados apenas no ModelName
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AircraftModel that = (AircraftModel) o;
        return Objects.equals(modelName, that.modelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelName);
    }

    public ModelName getModelName() {
        return this.modelName;
    }

    public String getModelPhotoUrl() {
        return this.modelPhotoUrl;
    }
}