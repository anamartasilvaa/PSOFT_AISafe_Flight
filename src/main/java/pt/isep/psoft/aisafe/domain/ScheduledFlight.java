package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ScheduledFlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Version
    private Long version;

    private LocalDateTime scheduledDateTime;
    private LocalDateTime actualDepartureDateTime;
    private LocalDateTime actualArrivalDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_pk")
    private Route route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_pk")
    private Aircraft aircraft;

    protected ScheduledFlight() {}

    // Getters básicos necessários para a lógica
    public FlightStatus getStatus() { return status; }
    public Aircraft getAircraft() { return aircraft; }
}