package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Version
    private Long version;

    @Embedded
    @AttributeOverride(name = "number", column = @Column(name = "registration_number", unique = true, nullable = false))
    private RegistrationNumber registrationNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_model_pk", nullable = false)
    private AircraftModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftStatus status;

    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @Column(nullable = false)
    private Integer actualSeatingCapacity;

    @Column(nullable = false)
    private Double totalFlightHours;

    @Column(nullable = true)
    private String features;

    protected Aircraft() {}

    public Aircraft(RegistrationNumber registrationNumber, AircraftModel model,
                    LocalDate manufacturingDate, Integer actualSeatingCapacity, String features) {

        Assert.notNull(registrationNumber, "Registration number must not be null.");
        Assert.notNull(model, "Aircraft model must not be null.");
        Assert.notNull(manufacturingDate, "Manufacturing date must not be null.");
        Assert.isTrue(actualSeatingCapacity != null && actualSeatingCapacity > 0,
                "Actual seating capacity must be strictly positive.");

        this.registrationNumber = registrationNumber;
        this.model = model;
        this.manufacturingDate = manufacturingDate;
        this.actualSeatingCapacity = actualSeatingCapacity;
        this.features = features;

        this.status = AircraftStatus.ACTIVE;
        this.totalFlightHours = 0.0;
    }

    public void updateStatus(AircraftStatus newStatus) {
        Assert.notNull(newStatus, "New status must not be null.");
        this.status = newStatus;
    }

    public void addFlightHours(Double hours) {
        Assert.isTrue(hours != null && hours > 0, "Flight hours to add must be positive.");
        this.totalFlightHours += hours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aircraft aircraft = (Aircraft) o;
        return Objects.equals(registrationNumber, aircraft.registrationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationNumber);
    }

    // --- GETTERS ---

    public RegistrationNumber getRegistrationNumber() {
        return this.registrationNumber;
    }

    public AircraftModel getAircraftModel() {
        return this.model;
    }

    public AircraftModel getModel() {
        return this.model;
    }

    public LocalDate getManufacturingDate() {
        return this.manufacturingDate;
    }

    public Integer getActualSeatingCapacity() {
        return this.actualSeatingCapacity;
    }

    public AircraftStatus getStatus() {
        return this.status;
    }

    public Double getTotalFlightHours() {
        return this.totalFlightHours;
    }

    public String getFeatures() {
        return this.features;
    }
}