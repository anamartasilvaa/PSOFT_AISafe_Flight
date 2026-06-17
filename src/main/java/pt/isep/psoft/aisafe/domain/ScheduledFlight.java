package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.time.LocalDateTime;

@Entity
public class ScheduledFlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Version
    private Long version;

    @Column(nullable = false)
    private LocalDateTime scheduledDateTime;

    private LocalDateTime actualDepartureDateTime;
    private LocalDateTime actualArrivalDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_pk", nullable = false)
    private Route route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_pk", nullable = false)
    private Aircraft aircraft;

    protected ScheduledFlight() {}

    // --- CONSTRUTOR COM REGRAS DE NEGÓCIO ---
    public ScheduledFlight(Route route, Aircraft aircraft, LocalDateTime scheduledDateTime) {
        Assert.notNull(route, "A route is required to schedule a flight.");
        Assert.notNull(aircraft, "An aircraft is required to schedule a flight.");
        Assert.notNull(scheduledDateTime, "The scheduled date and time are required.");
        Assert.isTrue(scheduledDateTime.isAfter(LocalDateTime.now()), "Cannot schedule a flight in the past.");

        this.route = route;
        this.aircraft = aircraft;
        this.scheduledDateTime = scheduledDateTime;

        // Estado inicial definido automaticamente
        this.status = FlightStatus.SCHEDULED;
    }

    // --- GETTERS ---
    public Long getPk() { return pk; }
    public Route getRoute() { return route; }
    public Aircraft getAircraft() { return aircraft; }
    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }
    public FlightStatus getStatus() { return status; }
}