package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
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

    // 3. Construtor exigido pelo JPA (Protegido)
    protected Airport() {}

    // 4. Construtor de Domínio (O único que usamos no nosso código)
    public Airport(IATACode iataCode, String name, AirportType type) {
        Assert.notNull(iataCode, "The IATA code is mandatory.");
        Assert.hasText(name, "The airport name is required..");
        Assert.notNull(type, "The type of airport is mandatory.");

        this.iataCode = iataCode;
        this.name = name;
        this.type = type;
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