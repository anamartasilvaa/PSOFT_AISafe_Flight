package pt.isep.psoft.aisafe.application.DTO;

public record BusiestAirportDTO(
        String iataCode,
        String name,
        long routeCount
) {}