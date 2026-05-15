package pt.isep.psoft.aisafe.application.DTO;

import pt.isep.psoft.aisafe.domain.MaintenanceType;

public record CreateMaintenanceTemplateDTO(
        String name,
        MaintenanceType type,
        String checklist
) {}