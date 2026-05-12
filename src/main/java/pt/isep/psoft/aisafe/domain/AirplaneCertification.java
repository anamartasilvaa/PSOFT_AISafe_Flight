package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.time.LocalDate;

@Entity
public class AirplaneCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(nullable = false)
    private String certificationNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_model_pk", nullable = false)
    private AircraftModel aircraftModel;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    protected AirplaneCertification() {}

    public AirplaneCertification(String certificationNumber, AircraftModel aircraftModel,
                                 LocalDate issueDate, LocalDate expiryDate) {
        Assert.hasText(certificationNumber, "Certification number must not be blank.");
        Assert.notNull(aircraftModel, "Aircraft model must not be null.");
        Assert.notNull(issueDate, "Issue date must not be null.");
        Assert.notNull(expiryDate, "Expiry date must not be null.");
        Assert.isTrue(expiryDate.isAfter(issueDate), "Expiry date must be after the issue date.");

        this.certificationNumber = certificationNumber;
        this.aircraftModel = aircraftModel;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
    }
}