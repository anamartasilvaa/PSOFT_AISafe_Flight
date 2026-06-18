package pt.isep.psoft.aisafe.application.DTO;

import java.util.List;

public record AlternativeRouteDTO(
        List<RouteViewDTO> legs, // As várias rotas que compõem a viagem
        Integer totalEstimatedFlightTime,
        Double totalDistance
) {
}