package pt.isep.psoft.aisafe.application.DTO;

public record AircraftModelViewDTO(
        String modelName,
        String manufacturer,
        Integer defaultSeatingCapacity,
        Double fuelCapacity,
        Double maximumRange,
        Double cruisingSpeed,
        String modelPhotoUrl,
        String seatingConfiguration,
        String operatingHoursRange,
        String engineType
) {}