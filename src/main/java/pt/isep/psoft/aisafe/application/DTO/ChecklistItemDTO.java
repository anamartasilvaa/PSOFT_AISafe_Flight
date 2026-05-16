package pt.isep.psoft.aisafe.application.DTO;

public record ChecklistItemDTO(
        String taskDescription,
        Boolean isMandatory
) {}