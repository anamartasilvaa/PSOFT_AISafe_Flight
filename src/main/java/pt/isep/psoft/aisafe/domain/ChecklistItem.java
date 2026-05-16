package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public class ChecklistItem {

    private String taskDescription;
    private Boolean isMandatory;

    protected ChecklistItem() {}

    public ChecklistItem(String taskDescription, Boolean isMandatory) {
        Assert.hasText(taskDescription, "Task description is required");
        Assert.notNull(isMandatory, "Mandatory flag is required");

        this.taskDescription = taskDescription;
        this.isMandatory = isMandatory;
    }

    public String getTaskDescription() { return taskDescription; }
    public Boolean getIsMandatory() { return isMandatory; }
}