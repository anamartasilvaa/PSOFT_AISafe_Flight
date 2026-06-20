package pt.isep.psoft.aisafe.application.DTO;

public record AircraftUtilizationRateDTO(
        String period, // Ex: "2026-06"
        Long flightCount
) {}