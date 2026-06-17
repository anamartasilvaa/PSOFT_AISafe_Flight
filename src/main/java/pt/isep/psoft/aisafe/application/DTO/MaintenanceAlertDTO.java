package pt.isep.psoft.aisafe.application.DTO;

public record MaintenanceAlertDTO(
        String registrationNumber,
        String alertType, // Pode ser "FLIGHT_HOURS" ou "CALENDAR_DAYS"
        String message
) {
}