package pt.isep.psoft.aisafe.application.DTO;

public record ScheduledFlightViewDTO(
        Long id,
        String routeId,
        String registrationNumber,
        String scheduledDateTime,
        String status
) {}