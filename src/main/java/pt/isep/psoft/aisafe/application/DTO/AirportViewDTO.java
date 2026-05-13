package pt.isep.psoft.aisafe.application.DTO;

import java.util.List;

public record AirportViewDTO(
        String iataCode,
        String name,
        String city,
        String country,
        String timezone,
        String type,
        List<RunwayDTO> runways,
        List<CertificationViewDTO> certifications // O Java vai buscar este tipo ao outro ficheiro automaticamente
) {}