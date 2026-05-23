package pt.isep.psoft.aisafe.application.DTO;

import java.time.LocalDateTime;

public record RouteHistoryDTO(
        String routeId,
        String action,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}