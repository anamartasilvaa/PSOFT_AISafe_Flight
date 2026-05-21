package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pt.isep.psoft.aisafe.domain.ComponentCategory;

public record RegisterMaintenanceDTO(

        @NotBlank(message = "Aircraft registration number is required.")
        String registrationNumber,

        @NotNull(message = "Template ID cannot be null.")
        @Min(value = 1, message = "Template ID must be greater than zero.")
        Long templateId,

        @NotBlank(message = "Maintenance description is required.")
        String description,

        @NotNull(message = "Expected duration cannot be null.")
        @Min(value = 1, message = "Expected duration must be at least 1 minute.")
        Integer expectedDuration,

        @NotNull(message = "Component category is required.")
        ComponentCategory componentCategory,

        @NotBlank(message = "Start date is required.")
        String startDate
) {}