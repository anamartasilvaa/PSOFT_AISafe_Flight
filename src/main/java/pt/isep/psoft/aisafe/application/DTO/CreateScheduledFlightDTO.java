package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.NotBlank;

public record CreateScheduledFlightDTO(
        @NotBlank(message = "Route ID is required.")
        String routeId,

        @NotBlank(message = "Aircraft registration number is required.")
        String registrationNumber,

        @NotBlank(message = "Scheduled date and time is required (ISO format: YYYY-MM-DDTHH:MM:SS).")
        String scheduledDateTime
) {}