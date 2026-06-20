package pt.isep.psoft.aisafe.application.DTO;

public record LowStockAlertDTO(
        String partNumber,
        String partName,
        Integer currentStock,
        Integer minimumThreshold,
        String alertMessage
) {}