package pt.isep.psoft.aisafe.application.DTO;

import jakarta.validation.constraints.Size;

public record UpdateAirportDetailsDTO(

        @Size(max = 100, message = "Operational hours description must not exceed 100 characters")
        String operationalHours,

        @Size(max = 200, message = "Contact information must not exceed 200 characters")
        String contactInformation
) {}