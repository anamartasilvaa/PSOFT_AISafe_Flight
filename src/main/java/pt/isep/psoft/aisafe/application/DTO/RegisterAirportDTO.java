package pt.isep.psoft.aisafe.application.DTO;

import java.util.List;

public record RegisterAirportDTO(
        String iataCode,
        String name,
        String city,
        String country,
        String timezone,
        String type,
        Double latitude,
        Double longitude,
        List<RunwayDTO> runways
) {}