package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.util.Objects;

@Entity
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Version
    private Long version;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "route_id", unique = true, nullable = false))
    private RouteId routeId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origin_pk")
    private Airport origin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_pk")
    private Airport destination;

    private Integer estimatedFlightTime;
    private Double minimumRange;
    private Integer minimumCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status = RouteStatus.ACTIVE;

    protected Route() {}

    public Route(RouteId routeId, Airport origin, Airport destination, Integer estimatedFlightTime, Double minimumRange, Integer minimumCapacity) {
        Assert.notNull(routeId, "Route ID cannot be null.");
        Assert.notNull(origin, "Origin airport is required.");
        Assert.notNull(destination, "Destination airport is required.");
        Assert.isTrue(!origin.equals(destination), "Origin and destination airports must be different.");

        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.estimatedFlightTime = estimatedFlightTime;
        this.minimumRange = minimumRange;
        this.minimumCapacity = minimumCapacity;
    }


    // GETTERS

    public RouteId getRouteId() { return routeId; }
    public Airport getOrigin() { return origin; }
    public Airport getDestination() { return destination; }
    public Integer getEstimatedFlightTime() { return estimatedFlightTime; }
    public Double getMinimumRange() { return minimumRange; }
    public Integer getMinimumCapacity() { return minimumCapacity; }
    public RouteStatus getStatus() { return status; }


    // MÉTODOS DE NEGÓCIO (US112)


    // US112 - Desativar a rota
    public void deactivate() {
        if (this.status == RouteStatus.INACTIVE) {
            throw new IllegalStateException("Esta rota já se encontra inativa!");
        }
        this.status = RouteStatus.INACTIVE;
    }

    // US112 - Atualizar os dados da rota
    public void updateParameters(Integer estimatedFlightTime, Double minimumRange, Integer minimumCapacity) {
        if (estimatedFlightTime != null) this.estimatedFlightTime = estimatedFlightTime;
        if (minimumRange != null) this.minimumRange = minimumRange;
        if (minimumCapacity != null) this.minimumCapacity = minimumCapacity;
    }
}