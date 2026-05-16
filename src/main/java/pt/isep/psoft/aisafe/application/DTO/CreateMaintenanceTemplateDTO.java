package pt.isep.psoft.aisafe.application.DTO;

import java.util.List;

public record CreateMaintenanceTemplateDTO(
        String templateName,
        String templateType,
        Double flightHoursInterval,
        Integer calendarDaysInterval,
        String checklistTitle,
        String checklistVersion,
        List<ChecklistItemDTO> checklistItems,
        List<Long> applicableModelIds
) {}