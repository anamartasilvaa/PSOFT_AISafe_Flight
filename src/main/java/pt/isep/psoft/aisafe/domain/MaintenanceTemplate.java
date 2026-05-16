package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MaintenanceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(unique = true, nullable = false)
    private String templateName;

    private String templateType;
    private Double flightHoursInterval;
    private Integer calendarDaysInterval;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "checklist_pk")
    private Checklist checklist;

    @ManyToMany
    @JoinTable(
            name = "template_aircraft_models",
            joinColumns = @JoinColumn(name = "template_pk"),
            inverseJoinColumns = @JoinColumn(name = "aircraft_model_pk")
    )
    private List<AircraftModel> appliesTo = new ArrayList<>();

    protected MaintenanceTemplate() {}

    public MaintenanceTemplate(String templateName, String templateType, Double flightHoursInterval, Integer calendarDaysInterval, Checklist checklist, List<AircraftModel> appliesTo) {
        Assert.hasText(templateName, "Template name is required");

        this.templateName = templateName;
        this.templateType = templateType;
        this.flightHoursInterval = flightHoursInterval;
        this.calendarDaysInterval = calendarDaysInterval;
        this.checklist = checklist;

        if (appliesTo != null) {
            this.appliesTo = appliesTo;
        }
    }

    public Long getPk() { return pk; }
    public String getTemplateName() { return templateName; }
    public String getTemplateType() { return templateType; }
    public Double getFlightHoursInterval() { return flightHoursInterval; }
    public Integer getCalendarDaysInterval() { return calendarDaysInterval; }
    public Checklist getChecklist() { return checklist; }
    public List<AircraftModel> getAppliesTo() { return appliesTo; }
}