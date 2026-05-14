package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AirportRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final AirportRepository airportRepository; // Precisamos disto para verificar se o aeroporto existe!

    public RouteService(RouteRepository routeRepository, AirportRepository airportRepository) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
    }

    public RouteViewDTO createRoute(CreateRouteDTO dto) {

        Airport origin = airportRepository.findByIataCodeString(dto.originIata())
                .orElseThrow(() -> new IllegalArgumentException("Origin Airport not found: " + dto.originIata()));


        Airport destination = airportRepository.findByIataCodeString(dto.destinationIata())
                .orElseThrow(() -> new IllegalArgumentException("Destination Airport not found: " + dto.destinationIata()));


        RouteId routeId = new RouteId(dto.routeId());
        Route newRoute = new Route(routeId, origin, destination, dto.estimatedFlightTime(), dto.minimumRange(), dto.minimumCapacity());


        Route savedRoute = routeRepository.save(newRoute);


        return new RouteViewDTO(
                savedRoute.getRouteId().id(),
                savedRoute.getOrigin().getIataCode().code(),
                savedRoute.getDestination().getIataCode().code(),
                savedRoute.getStatus().name()
        );
    }
}