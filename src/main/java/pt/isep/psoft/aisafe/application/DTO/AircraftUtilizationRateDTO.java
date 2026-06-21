package pt.isep.psoft.aisafe.application.DTO;

public record AircraftUtilizationRateDTO(
        String registrationNumber, // Ex: "CS-TPA"
        String period,             // Ex: "2026-06"
        Long flightCount           // Ex: 15
) {}