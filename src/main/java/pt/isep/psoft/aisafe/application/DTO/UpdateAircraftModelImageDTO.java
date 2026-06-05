package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UpdateAircraftModelImageDTO(
        @NotBlank(message = "Image URL cannot be empty")
        @URL(message = "Must be a valid URL format")
        String imageUrl
) {}