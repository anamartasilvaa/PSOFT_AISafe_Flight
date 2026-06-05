package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record UpdateAircraftModelSpecsDTO(
        @Min(value = 1, message = "Seating capacity must be at least 1")
        Integer seatingCapacity,

        @Positive(message = "Fuel capacity must be greater than zero")
        Double fuelCapacity,

        @Positive(message = "Maximum range must be strictly positive")
        Double maximumRange,

        @Positive(message = "Cruising speed must be strictly positive")
        Double cruisingSpeed,

        String seatingConfiguration,
        String operatingHoursRange
) {}