package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.util.Objects;

@Entity
public class AircraftModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Version
    private Long version;

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

    @Column(nullable = true)
    private String modelPhotoUrl;

    @Column(nullable = true)
    private String operatingHoursRange;

    @Column(nullable = true)
    private String seatingConfiguration;

    @Column(nullable = true)
    private String engineType;

    protected AircraftModel() {}

    public AircraftModel(ModelName modelName, Manufacturer manufacturer, Integer defaultSeatingCapacity,
                         Double fuelCapacity, Double maximumRange, Double cruisingSpeed, String modelPhotoUrl,
                         String seatingConfiguration, String operatingHoursRange, String engineType) {

        Assert.notNull(modelName, "The model name is required.");
        Assert.notNull(manufacturer, "The manufacturer is required.");
        Assert.isTrue(defaultSeatingCapacity != null && defaultSeatingCapacity > 0, "The seating capacity must be greater than 0.");
        Assert.isTrue(fuelCapacity != null && fuelCapacity > 0, "The fuel capacity must be greater than 0.");
        Assert.isTrue(maximumRange != null && maximumRange > 0, "The maximum range must be greater than 0.");
        Assert.isTrue(cruisingSpeed != null && cruisingSpeed > 0, "The cruising speed must be greater than 0.");

        this.modelName = modelName;
        this.manufacturer = manufacturer;
        this.defaultSeatingCapacity = defaultSeatingCapacity;
        this.fuelCapacity = fuelCapacity;
        this.maximumRange = maximumRange;
        this.cruisingSpeed = cruisingSpeed;
        this.modelPhotoUrl = modelPhotoUrl;
        this.seatingConfiguration = seatingConfiguration;
        this.operatingHoursRange = operatingHoursRange;
        this.engineType = engineType;
    }

    public AircraftModel(ModelName name, Manufacturer manufacturer, int i, double v, double v1, double v2, String url, String s, Object o) {
    }

    // US201
    public void updateSpecifications(Integer newSeatingCapacity, Double newFuelCapacity,
                                     Double newMaximumRange, Double newCruisingSpeed,
                                     String newSeatingConfiguration, String newOperatingHoursRange) {
        if (newSeatingCapacity != null) {
            Assert.isTrue(newSeatingCapacity > 0, "Seating capacity must be greater than 0.");
            this.defaultSeatingCapacity = newSeatingCapacity;
        }
        if (newFuelCapacity != null) {
            Assert.isTrue(newFuelCapacity > 0, "Fuel capacity must be greater than 0.");
            this.fuelCapacity = newFuelCapacity;
        }
        if (newMaximumRange != null) {
            Assert.isTrue(newMaximumRange > 0, "Maximum range must be greater than 0.");
            this.maximumRange = newMaximumRange;
        }
        if (newCruisingSpeed != null) {
            Assert.isTrue(newCruisingSpeed > 0, "Cruising speed must be greater than 0.");
            this.cruisingSpeed = newCruisingSpeed;
        }
        if (newSeatingConfiguration != null) this.seatingConfiguration = newSeatingConfiguration;
        if (newOperatingHoursRange != null) this.operatingHoursRange = newOperatingHoursRange;
    }

    // US202
    public void updateImage(String newImageUrl) {
        this.modelPhotoUrl = newImageUrl;
    }

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

    // Getters
    public ModelName getModelName() { return this.modelName; }
    public Manufacturer getManufacturer() { return this.manufacturer; }
    public Integer getDefaultSeatingCapacity() { return this.defaultSeatingCapacity; }
    public Double getFuelCapacity() { return this.fuelCapacity; }
    public Double getMaximumRange() { return this.maximumRange; }
    public Double getCruisingSpeed() { return this.cruisingSpeed; }
    public String getModelPhotoUrl() { return this.modelPhotoUrl; }
    public String getSeatingConfiguration() { return this.seatingConfiguration; }
    public String getOperatingHoursRange() { return this.operatingHoursRange; }

    public String getEngineType() { return this.engineType; }
}