package pt.isep.psoft.aisafe.application.DTO;

public record CreateRouteDTO(
        String routeId,
        String originIata,
        String destinationIata,
        Integer estimatedFlightTime,
        Double minimumRange,
        Integer minimumCapacity
) {}