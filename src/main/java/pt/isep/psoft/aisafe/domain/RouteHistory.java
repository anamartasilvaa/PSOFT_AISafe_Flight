package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.time.LocalDateTime;

@Entity
public class RouteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(nullable = false)
    private String routeId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    protected RouteHistory() {}

    public RouteHistory(String routeId, String action) {
        Assert.hasText(routeId, "Route ID required");
        Assert.hasText(action, "Action required");
        this.routeId = routeId;
        this.action = action;
        this.startDate = LocalDateTime.now();
    }

    public void closeHistory(LocalDateTime endDate) {
        Assert.notNull(endDate, "End date is required");
        Assert.isTrue(endDate.isAfter(this.startDate), "End date must be after start date");
        this.endDate = endDate;
    }

    public String getRouteId() { return routeId; }
    public String getAction() { return action; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public boolean isActive() { return endDate == null; }
}