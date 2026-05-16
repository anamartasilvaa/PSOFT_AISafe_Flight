package pt.isep.psoft.aisafe.application.DTO;

import pt.isep.psoft.aisafe.domain.ComponentCategory;

public record RegisterMaintenanceDTO(
        String registrationNumber,
        Long templateId,
        String description,
        Integer expectedDuration,
        ComponentCategory componentCategory

) {}