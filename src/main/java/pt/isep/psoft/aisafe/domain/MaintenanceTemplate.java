package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;

@Entity
public class MaintenanceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private MaintenanceType type;

    @Column(length = 2000)
    private String checklist;

    protected MaintenanceTemplate() {}

    public MaintenanceTemplate(String name, MaintenanceType type, String checklist) {
        Assert.hasText(name, "O nome do template é obrigatório");
        Assert.notNull(type, "O tipo de manutenção é obrigatório");

        this.name = name;
        this.type = type;
        this.checklist = checklist;
    }

    // Getters
    public String getName() { return name; }
    public MaintenanceType getType() { return type; }
    public String getChecklist() { return checklist; }
}