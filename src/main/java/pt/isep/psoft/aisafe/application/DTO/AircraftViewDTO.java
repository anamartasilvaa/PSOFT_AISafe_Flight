package pt.isep.psoft.aisafe.application.DTO;
import java.time.LocalDate;

public record AircraftViewDTO(
        String registrationNumber,
        String modelName,
        LocalDate manufacturingDate,
        Integer seatingCapacity,
        String status
) {}