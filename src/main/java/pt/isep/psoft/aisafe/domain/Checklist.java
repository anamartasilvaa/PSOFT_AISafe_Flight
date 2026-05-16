package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.*;
import org.springframework.util.Assert;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    private String title;
    private String version;

    @ElementCollection
    @CollectionTable(name = "checklist_items", joinColumns = @JoinColumn(name = "checklist_pk"))
    private List<ChecklistItem> items = new ArrayList<>();

    protected Checklist() {}

    public Checklist(String title, String version, List<ChecklistItem> items) {
        Assert.hasText(title, "Title is required");
        Assert.hasText(version, "Version is required");

        this.title = title;
        this.version = version;
        if (items != null) {
            this.items = items;
        }
    }

    public Long getPk() { return pk; }
    public String getTitle() { return title; }
    public String getVersion() { return version; }
    public List<ChecklistItem> getItems() { return items; }
}