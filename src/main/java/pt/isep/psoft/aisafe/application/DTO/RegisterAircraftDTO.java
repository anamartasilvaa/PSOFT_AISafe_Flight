package pt.isep.psoft.aisafe.application.DTO;

import java.time.LocalDate;

public record RegisterAircraftDTO(
        String registrationNumber,
        String modelName,
        LocalDate manufacturingDate,
        Integer seatingCapacity,
        String features
) {}