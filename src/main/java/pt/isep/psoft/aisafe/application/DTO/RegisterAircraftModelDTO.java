package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.URL;

public record RegisterAircraftModelDTO(
        @NotBlank(message = "Model name is required")
        String modelName,

        @NotBlank(message = "Manufacturer is required")
        String manufacturer,

        @Min(value = 1, message = "Seating capacity must be at least 1")
        Integer seatingCapacity,

        @Positive(message = "Fuel capacity must be greater than zero")
        Double fuelCapacity,

        @Positive(message = "Range must be strictly positive")
        Double range,

        @Positive(message = "Speed must be strictly positive")
        Double speed,

        @URL(message = "If provided, the model photo must be a valid URL")
        String modelPhotoUrl
) {}