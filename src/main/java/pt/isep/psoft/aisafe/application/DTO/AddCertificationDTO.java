package pt.isep.psoft.aisafe.application.DTO;

import java.time.LocalDate;

public record AddCertificationDTO(
        String certificationNumber,
        String modelName,
        LocalDate issueDate,
        LocalDate expiryDate
) {}