package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateRouteDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AirportRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;
import pt.isep.psoft.aisafe.repositories.RouteHistoryRepository;

import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final RouteHistoryRepository routeHistoryRepository;

    public RouteService(RouteRepository routeRepository,
                        AirportRepository airportRepository,
                        RouteHistoryRepository routeHistoryRepository) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
        this.routeHistoryRepository = routeHistoryRepository;
    }

    // --- US110: Criar uma Rota ---
    public RouteViewDTO createRoute(CreateRouteDTO dto) {
        Airport origin = airportRepository.findByIataCodeString(dto.originIata())
                .orElseThrow(() -> new IllegalArgumentException("Origin Airport not found: " + dto.originIata()));

        Airport destination = airportRepository.findByIataCodeString(dto.destinationIata())
                .orElseThrow(() -> new IllegalArgumentException("Destination Airport not found: " + dto.destinationIata()));

        RouteId routeId = new RouteId(dto.routeId());
        Route newRoute = new Route(routeId, origin, destination, dto.estimatedFlightTime(), dto.minimumRange(), dto.minimumCapacity());

        Route savedRoute = routeRepository.save(newRoute);

        routeHistoryRepository.save(new RouteHistory(savedRoute.getRouteId().id(), "CREATED"));

        return convertToDTO(savedRoute);
    }

    // --- US112: Desativar uma rota ---
    public RouteViewDTO deactivateRoute(String id) {
        RouteId routeId = new RouteId(id);

        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));

        route.deactivate();
        Route saved = routeRepository.save(route);

        routeHistoryRepository.save(new RouteHistory(saved.getRouteId().id(), "DEACTIVATED"));

        return convertToDTO(saved);
    }

    // --- US112: Atualizar uma rota ---
    public RouteViewDTO updateRoute(String id, UpdateRouteDTO dto) {
        RouteId routeId = new RouteId(id);

        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));

        route.updateParameters(dto.estimatedFlightTime(), dto.minimumRange(), dto.minimumCapacity());

        Route saved = routeRepository.save(route);

        routeHistoryRepository.save(new RouteHistory(saved.getRouteId().id(), "UPDATED"));

        return convertToDTO(saved);
    }

    // --- US113: Ver Detalhes de uma Rota Específica (NOVO MÉTODO) ---
    public RouteViewDTO getRoute(String id) {
        RouteId routeId = new RouteId(id);
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));

        return convertToDTO(route);
    }

    // --- US113 e US114: Pesquisar Rotas ---
    public List<RouteViewDTO> searchRoutes(String origin, String destination) {
        List<Route> results;

        if (origin != null && destination != null) {
            results = routeRepository.findByOrigin_IataCode_CodeAndDestination_IataCode_Code(origin, destination);
        } else if (origin != null) {
            results = routeRepository.findByOrigin_IataCode_Code(origin);
        } else if (destination != null) {
            results = routeRepository.findByDestination_IataCode_Code(destination);
        } else {
            results = routeRepository.findAll();
        }

        return results.stream().map(this::convertToDTO).toList();
    }

    private RouteViewDTO convertToDTO(Route route) {
        return new RouteViewDTO(
                route.getRouteId().id(),
                route.getOrigin().getIataCode().code(),
                route.getDestination().getIataCode().code(),
                route.getStatus().name()
        );
    }
}