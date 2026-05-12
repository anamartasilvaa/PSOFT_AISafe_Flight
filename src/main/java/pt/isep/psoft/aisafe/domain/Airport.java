package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Airport {

    // 1. Identidade técnica (Base de Dados)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    // 2. Identidade de Domínio (Value Object embebido)
    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "iata_code", unique = true, nullable = false))
    private IATACode iataCode;

    @Column(nullable = false)
    private String name;

    // CORREÇÃO AQUI: Como é um Enum, usamos @Enumerated
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

    // 3. Construtor exigido pelo JPA (Protegido)
    protected Airport() {}

    // 4. Construtor de Domínio (O único que usamos no nosso código)
    // 4. Domain Constructor (Updated to include Coordinates)
    public Airport(IATACode iataCode, String name, AirportType type, Coordinates coordinates) {
        Assert.notNull(iataCode, "The IATA code is mandatory.");
        Assert.hasText(name, "The airport name is required.");
        Assert.notNull(type, "The type of airport is mandatory.");
        Assert.notNull(coordinates, "Coordinates are required.");

        this.iataCode = iataCode;
        this.name = name;
        this.type = type;
        this.coordinates = coordinates;
    }

    public void addRunway(Runway runway) {
        Assert.notNull(runway, "Runway must not be null.");
        this.runways.add(runway);
    }

    public void addAirplaneCertification(AirplaneCertification certification) {
        Assert.notNull(certification, "Certification must not be null.");
        this.certifications.add(certification);
    }

    // 5. equals e hashCode baseados apenas no IATACode (Regra DDD)
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
}