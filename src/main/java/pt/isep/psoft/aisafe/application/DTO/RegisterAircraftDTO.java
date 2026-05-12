package pt.isep.psoft.aisafe.application.DTO;
import java.time.LocalDate;

public record RegisterAircraftDTO(
        String registrationNumber,
        String modelName, // Usamos o nome para ir à base de dados procurar o molde
        LocalDate manufacturingDate,
        Integer seatingCapacity
) {}