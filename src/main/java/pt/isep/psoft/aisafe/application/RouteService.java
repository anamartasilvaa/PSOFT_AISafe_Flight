package pt.isep.psoft.aisafe.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AirportRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;
import pt.isep.psoft.aisafe.repositories.RouteHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository;
    private final RouteHistoryRepository routeHistoryRepository;
    private final RouteSearchStrategy routeSearchStrategy; // <-- A Estratégia adicionada aqui

    public RouteService(RouteRepository routeRepository,
                        AirportRepository airportRepository,
                        RouteHistoryRepository routeHistoryRepository,
                        RouteSearchStrategy routeSearchStrategy) { // <-- E aqui no construtor
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
        this.routeHistoryRepository = routeHistoryRepository;
        this.routeSearchStrategy = routeSearchStrategy;
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
        closeOldHistoryAndCreateNew(saved.getRouteId().id(), "DEACTIVATED");

        return convertToDTO(saved);
    }

    // --- US112: Atualizar uma rota ---
    public RouteViewDTO updateRoute(String id, UpdateRouteDTO dto) {
        RouteId routeId = new RouteId(id);
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));

        route.updateParameters(dto.estimatedFlightTime(), dto.minimumRange(), dto.minimumCapacity());
        Route saved = routeRepository.save(route);
        closeOldHistoryAndCreateNew(saved.getRouteId().id(), "UPDATED");

        return convertToDTO(saved);
    }

    // --- US113: Ver Detalhes de uma Rota Específica ---
    public RouteViewDTO getRoute(String id) {
        RouteId routeId = new RouteId(id);
        Route route = routeRepository.findByRouteId(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));

        return convertToDTO(route);
    }

    // --- US113 e US114: Pesquisar Rotas  ---
    public Page<RouteViewDTO> searchRoutes(String origin, String destination, Pageable pageable) {
        Page<Route> results;

        if (origin != null && destination != null) {
            results = routeRepository.findByOrigin_IataCode_CodeAndDestination_IataCode_Code(origin, destination, pageable);
        } else if (origin != null) {
            results = routeRepository.findByOrigin_IataCode_Code(origin, pageable);
        } else if (destination != null) {
            results = routeRepository.findByDestination_IataCode_Code(destination, pageable);
        } else {
            results = routeRepository.findAll(pageable);
        }

        return results.map(this::convertToDTO);
    }

    // --- US111: Ver Histórico da Rota ---
    public List<RouteHistoryDTO> getRouteHistory(String routeId) {
        List<RouteHistory> entities = routeHistoryRepository.findByRouteIdOrderByStartDateDesc(routeId);
        return entities.stream().map(entity ->
                new RouteHistoryDTO(
                        entity.getRouteId(),
                        entity.getAction(),
                        entity.getStartDate(),
                        entity.getEndDate()
                )
        ).collect(Collectors.toList());
    }

    //US209
    public Page<RouteViewDTO> getRoutesByAirport(String iataCode, Pageable pageable) {
        String code = iataCode.toUpperCase();
        Page<Route> routes = routeRepository.findRoutesByAirport(code, pageable);
        return routes.map(this::convertToDTO);
    }

    // US210
    public List<pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO> getBusiestAirports() {
        List<Airport> airports = airportRepository.findAll();

        return airports.stream()
                .map(airport -> {
                    String iata = airport.getIataCode().code();
                    long count = routeRepository.countRoutesForAirport(iata);
                    return new pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO(iata, airport.getName(), count);
                })
                .sorted((a1, a2) -> Long.compare(a2.routeCount(), a1.routeCount()))
                .collect(Collectors.toList());
    }

    // --- US215: Distância total da rede ---
    public Double getTotalNetworkDistance() {
        Double total = routeRepository.calculateTotalNetworkDistance();
        return (total != null) ? total : 0.0;
    }

    // --- US214: Listar rotas ativas ordenadas por popularidade ou distância ---
    public Page<RouteViewDTO> getActiveRoutesSorted(String sortBy, Pageable pageable) {
        Page<Route> routes;

        // Decide a ordenação baseada no parâmetro recebido
        if ("popularity".equalsIgnoreCase(sortBy)) {
            routes = routeRepository.findAllActiveRoutesSortedByPopularity(pageable);
        } else {
            routes = routeRepository.findAllActiveRoutesSortedByDistance(pageable);
        }

        // Usa o teu próprio método convertToDTO em vez de criar um DTO novo à mão!
        return routes.map(this::convertToDTO);
    }

    // --- US216: Procurar rotas alternativas ---
    public List<AlternativeRouteDTO> findAlternativeRoutes(String originIata, String destinationIata) {
        return routeSearchStrategy.findAlternatives(originIata, destinationIata);
    }

    // --- Métodos Privados Auxiliares ---

    private RouteViewDTO convertToDTO(Route route) {
        return new RouteViewDTO(
                route.getRouteId().id(),
                route.getOrigin().getIataCode().code(),
                route.getDestination().getIataCode().code(),
                route.getStatus().name(),
                route.getMinimumCapacity()
        );
    }

    private void closeOldHistoryAndCreateNew(String routeId, String action) {
        Optional<RouteHistory> activeHistory = routeHistoryRepository.findFirstByRouteIdAndEndDateIsNull(routeId);
        if (activeHistory.isPresent()) {
            RouteHistory old = activeHistory.get();
            old.closeHistory(LocalDateTime.now());
            routeHistoryRepository.save(old);
        }
        routeHistoryRepository.save(new RouteHistory(routeId, action));
    }
}