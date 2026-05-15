package pt.isep.psoft.aisafe.application.DTO;

public record UpdateRouteDTO(
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity
) {}