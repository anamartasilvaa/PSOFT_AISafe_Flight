package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.time.LocalDate;

@Entity
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Version
    private Long version;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Integer expectedDuration;

    private String completionNotes;

    @Column(nullable = false)
    private Double cost; // Novo campo para US220

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceRecordStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComponentCategory componentCategory;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_pk")
    private Aircraft aircraft;

    @ManyToOne(optional = false)
    @JoinColumn(name = "template_pk")
    private MaintenanceTemplate template;

    protected MaintenanceRecord() {}

    public MaintenanceRecord(Aircraft aircraft, MaintenanceTemplate template, String description,
                             Integer expectedDuration, ComponentCategory componentCategory,
                             LocalDate startDate, Double cost) { // Adicionado cost
        Assert.notNull(aircraft, "Aircraft is required.");
        Assert.notNull(template, "Template is required.");
        Assert.hasText(description, "Description is required.");
        Assert.notNull(expectedDuration, "Expected duration is required.");
        Assert.notNull(componentCategory, "Component category is required.");
        Assert.notNull(startDate, "Start date is required.");
        Assert.notNull(cost, "Cost is required.");
        Assert.isTrue(cost >= 0, "Cost cannot be negative.");

        this.aircraft = aircraft;
        this.template = template;
        this.description = description;
        this.expectedDuration = expectedDuration;
        this.componentCategory = componentCategory;
        this.startDate = startDate;
        this.cost = cost;
        this.status = MaintenanceRecordStatus.SCHEDULED;
    }

    public void complete(String completionNotes) {
        Assert.hasText(completionNotes, "Completion notes are required.");
        this.status = MaintenanceRecordStatus.COMPLETED;
        this.completionNotes = completionNotes;
    }

    public Long getPk() { return pk; }
    public Long getVersion() { return version; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public Integer getExpectedDuration() { return expectedDuration; }
    public String getCompletionNotes() { return completionNotes; }
    public Double getCost() { return cost; } // Novo getter

    public MaintenanceRecordStatus getStatus() { return status; }
    public ComponentCategory getComponentCategory() { return componentCategory; }
    public Aircraft getAircraft() { return aircraft; }
    public MaintenanceTemplate getTemplate() { return template; }
}