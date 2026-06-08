package pt.isep.psoft.aisafe.application.DTO;

import java.util.List;

public record AirportViewDTO(
        String iataCode,
        String name,
        String city,
        String country,
        String timezone,
        String type,
        String status,
        List<RunwayDTO> runways,
        List<CertificationViewDTO> certifications,

        List<FacilityDTO> facilities,

        String imageUrl,
        String operationalHours,
        String contactInformation
) {}