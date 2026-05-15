package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RouteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(nullable = false)
    private String routeId;

    @Column(nullable = false)
    private String action; // Ex: "CREATED", "DEACTIVATED"

    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected RouteHistory() {}

    public RouteHistory(String routeId, String action) {
        this.routeId = routeId;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    public String getRouteId() { return routeId; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }
}