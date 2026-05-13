package pt.isep.psoft.aisafe.application.DTO;
import java.time.LocalDate;

public record CertificationViewDTO(
        String certificationNumber,
        String modelName,
        LocalDate expiryDate
) {}