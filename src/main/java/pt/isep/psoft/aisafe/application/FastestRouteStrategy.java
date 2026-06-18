package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.DTO.AlternativeRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.domain.Route;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

import java.util.*;

@Component
public class FastestRouteStrategy implements RouteSearchStrategy {

    private final RouteRepository routeRepository;
    private static final int MINIMUM_CONNECTING_TIME_MINUTES = 60; // A regra de 1 hora de espera obrigatória
    private static final int MAX_FLIGHTS = 3; // Limite fixo: máximo de 3 voos (ou seja, 2 escalas).

    public FastestRouteStrategy(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public List<AlternativeRouteDTO> findAlternatives(String originIata, String destinationIata) {
        List<AlternativeRouteDTO> foundAlternatives = new ArrayList<>();

        // Fila que vai guardar os caminhos que estamos a explorar
        Queue<List<Route>> queue = new LinkedList<>();

        // 1. Procurar os voos iniciais (sem escalas)
        List<Route> initialRoutes = getOutgoingRoutes(originIata);
        for (Route route : initialRoutes) {
            List<Route> initialPath = new ArrayList<>();
            initialPath.add(route);
            queue.add(initialPath);
        }

        // 2. Procurar ligações
        while (!queue.isEmpty()) {
            List<Route> currentPath = queue.poll();
            Route lastRoute = currentPath.get(currentPath.size() - 1);
            String currentAirport = lastRoute.getDestination().getIataCode().code();

            // Se chegámos ao destino, convertemos para DTO e guardamos a solução!
            if (currentAirport.equalsIgnoreCase(destinationIata)) {
                foundAlternatives.add(convertToAlternativeDTO(currentPath));
                continue;
            }

            // O limite mágico: Se já temos 3 voos e ainda não chegámos ao destino, descartamos este caminho.
            if (currentPath.size() >= MAX_FLIGHTS) {
                continue;
            }

            // Procurar os próximos voos a partir do aeroporto onde estamos
            List<Route> nextRoutes = getOutgoingRoutes(currentAirport);
            for (Route nextRoute : nextRoutes) {
                // Prevenção básica de ciclo (ex: Porto -> Madrid -> Porto)
                if (!isAirportInPath(currentPath, nextRoute.getDestination().getIataCode().code())) {
                    List<Route> newPath = new ArrayList<>(currentPath);
                    newPath.add(nextRoute);
                    queue.add(newPath);
                }
            }
        }

        // 3. Ordenar os resultados do Mais Rápido para o Mais Lento (A magia desta estratégia!)
        foundAlternatives.sort(Comparator.comparingInt(AlternativeRouteDTO::totalEstimatedFlightTime));

        return foundAlternatives;
    }

    // --- Métodos Auxiliares ---

    private List<Route> getOutgoingRoutes(String iataCode) {
        return routeRepository.findByOrigin_IataCode_Code(iataCode.toUpperCase(), org.springframework.data.domain.Pageable.unpaged())
                .filter(r -> r.getStatus() == pt.isep.psoft.aisafe.domain.RouteStatus.ACTIVE)
                .toList();
    }

    private boolean isAirportInPath(List<Route> path, String destinationIata) {
        for (Route r : path) {
            if (r.getOrigin().getIataCode().code().equalsIgnoreCase(destinationIata)) return true;
        }
        return false;
    }

    private AlternativeRouteDTO convertToAlternativeDTO(List<Route> path) {
        List<RouteViewDTO> legDTOs = new ArrayList<>();
        int totalTime = 0;
        double totalDistance = 0.0;

        for (int i = 0; i < path.size(); i++) {
            Route route = path.get(i);
            legDTOs.add(new RouteViewDTO(
                    route.getRouteId().id(),
                    route.getOrigin().getIataCode().code(),
                    route.getDestination().getIataCode().code(),
                    route.getStatus().name(),
                    route.getMinimumCapacity()
            ));

            // Soma o tempo do voo
            totalTime += (route.getEstimatedFlightTime() != null) ? route.getEstimatedFlightTime() : 0;
            totalDistance += (route.getMinimumRange() != null) ? route.getMinimumRange() : 0.0;

            // Se não for o último voo da viagem, soma o tempo de espera no aeroporto (MCT)
            if (i < path.size() - 1) {
                totalTime += MINIMUM_CONNECTING_TIME_MINUTES;
            }
        }

        return new AlternativeRouteDTO(legDTOs, totalTime, totalDistance);
    }
}