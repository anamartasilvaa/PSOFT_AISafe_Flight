package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.Min;

public record UpdateRouteDTO(
        @Min(value = 1, message = "Flight time must be positive")
        Integer estimatedFlightTime,

        @Min(value = 1, message = "Range must be positive")
        Double minimumRange,

        @Min(value = 1, message = "Capacity must be positive")
        Integer minimumCapacity
) {}