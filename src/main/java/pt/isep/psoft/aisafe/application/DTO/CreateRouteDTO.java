package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateRouteDTO(
        @NotBlank(message = "Route ID is mandatory")
        String routeId,

        @NotBlank(message = "Origin IATA is mandatory")
        @Size(min = 3, max = 3, message = "IATA must have 3 characters")
        String originIata,

        @NotBlank(message = "Destination IATA is mandatory")
        @Size(min = 3, max = 3, message = "IATA must have 3 characters")
        String destinationIata,

        @NotNull(message = "Estimated flight time is required")
        @Min(value = 1, message = "Flight time must be positive")
        Integer estimatedFlightTime,

        @NotNull(message = "Minimum range is required")
        @Min(value = 1, message = "Range must be positive")
        Double minimumRange,

        @NotNull(message = "Minimum capacity is required")
        @Min(value = 1, message = "Capacity must be positive")
        Integer minimumCapacity
) {}