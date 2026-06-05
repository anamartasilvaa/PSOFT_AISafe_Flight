package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Airport {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Version
    private Long version;


    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "iata_code", unique = true, nullable = false))
    private IATACode iataCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirportType type;

    @Embedded
    private Coordinates coordinates;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "airport_pk")
    private List<Runway> runways = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "airport_pk")
    private List<AirplaneCertification> certifications = new ArrayList<>();

    @Column(nullable = true)
    private String imageUrl;

    public void updateImage(String newImageUrl) {
        this.imageUrl = newImageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    protected Airport() {}

    public Airport(IATACode iataCode, String name, String city, String country, String timezone, AirportType type, Coordinates coordinates) {
        Assert.notNull(iataCode, "The IATA code is mandatory.");
        Assert.hasText(name, "The airport name is required.");
        Assert.hasText(city, "The city is required.");
        Assert.hasText(country, "The country is required.");
        Assert.hasText(timezone, "The timezone is required.");
        Assert.notNull(type, "The type of airport is mandatory.");
        Assert.notNull(coordinates, "Coordinates are required.");

        this.iataCode = iataCode;
        this.name = name;
        this.city = city;
        this.country = country;
        this.timezone = timezone;
        this.type = type;
        this.coordinates = coordinates;
    }


    public void addRunway(Runway runway) {
        Assert.notNull(runway, "Runway must not be null.");
        this.runways.add(runway);
    }

    public void addOrUpdateAirplaneCertification(AirplaneCertification newCertification) {
        Assert.notNull(newCertification, "Certification must not be null.");

        // 1. Remove a certificação antiga se for para o mesmo modelo de avião
        this.certifications.removeIf(existingCert ->
                existingCert.getAircraftModel().getModelName().equals(newCertification.getAircraftModel().getModelName())
        );

        // 2. Adiciona a nova certificação (o JPA trata de apagar a antiga na BD graças ao orphanRemoval)
        this.certifications.add(newCertification);
    }


    public IATACode getIataCode() { return iataCode; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getTimezone() { return timezone; }
    public AirportType getType() { return type; }
    public List<Runway> getRunways() { return runways; }
    public List<AirplaneCertification> getCertifications() { return certifications; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(iataCode, airport.iataCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iataCode);
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AirportStatus status = AirportStatus.OPERATIONAL;

    public void updateStatus(AirportStatus newStatus) {
        Assert.notNull(newStatus, "Status cannot be null.");
        this.status = newStatus;
    }

    public AirportStatus getStatus() { return status; }


    @ElementCollection
    private List<Facility> facilities = new ArrayList<>();

    public void addFacility(Facility facility) {
        Assert.notNull(facility, "Facility cannot be null.");
        this.facilities.add(facility);
    }

    public List<Facility> getFacilities() {
        return facilities;
    }
}