package pt.isep.psoft.aisafe.application.DTO;

public record FuelEfficiencyDTO(
        String identifier, // Pode ser o registrationNumber (se for por aeronave) ou routeId (se for por rota)
        Double averageFuelConsumption
) {}