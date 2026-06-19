package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.DTO.AlternativeRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.domain.Route;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FastestRouteStrategy implements RouteSearchStrategy {

    private final RouteRepository routeRepository;
    private static final int MINIMUM_CONNECTING_TIME_MINUTES = 60; // Regra do MCT (Minimum Connection Time)
    private static final int MAX_FLIGHTS = 3; // Limite fixo: máximo de 3 voos (2 escalas).

    public FastestRouteStrategy(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public List<AlternativeRouteDTO> findAlternatives(String originIata, String destinationIata) {
        List<AlternativeRouteDTO> foundAlternatives = new ArrayList<>();
        Queue<List<Route>> queue = new LinkedList<>();

        System.out.println("\n[ALGORITHM] Starting search for fastest alternative routes from " + originIata + " to " + destinationIata);

        // 1. Procurar os voos iniciais (sem escalas)
        List<Route> initialRoutes = getOutgoingRoutes(originIata);
        for (Route route : initialRoutes) {
            List<Route> initialPath = new ArrayList<>();
            initialPath.add(route);
            queue.add(initialPath);
        }

        // 2. Procurar ligações usando Breadth-First Search (BFS)
        while (!queue.isEmpty()) {
            List<Route> currentPath = queue.poll();
            Route lastRoute = currentPath.get(currentPath.size() - 1);
            String currentAirport = lastRoute.getDestination().getIataCode().code();

            // Se chegámos ao destino, processar a rota e guardar
            if (currentAirport.equalsIgnoreCase(destinationIata)) {
                AlternativeRouteDTO dto = convertToAlternativeDTO(currentPath);
                foundAlternatives.add(dto);

                // Imprime a árvore de decisão na consola para a defesa
                String pathString = currentPath.stream()
                        .map(r -> r.getOrigin().getIataCode().code())
                        .collect(Collectors.joining(" -> ")) + " -> " + destinationIata;
                System.out.println("[ALGORITHM] Found Valid Path: " + pathString + " | Total Cost: " + dto.totalEstimatedFlightTime() + " mins (includes MCT penalties)");
                continue;
            }

            // O limite mágico para evitar loops infinitos
            if (currentPath.size() >= MAX_FLIGHTS) {
                continue;
            }

            // Expandir a pesquisa a partir deste aeroporto
            List<Route> nextRoutes = getOutgoingRoutes(currentAirport);
            for (Route nextRoute : nextRoutes) {
                if (!isAirportInPath(currentPath, nextRoute.getDestination().getIataCode().code())) {
                    List<Route> newPath = new ArrayList<>(currentPath);
                    newPath.add(nextRoute);
                    queue.add(newPath);
                }
            }
        }

        // 3. Ordenar os resultados do Mais Rápido para o Mais Lento
        foundAlternatives.sort(Comparator.comparingInt(AlternativeRouteDTO::totalEstimatedFlightTime));

        System.out.println("[ALGORITHM] Search finished. Returned " + foundAlternatives.size() + " options sorted by speed.\n");

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

            totalTime += (route.getEstimatedFlightTime() != null) ? route.getEstimatedFlightTime() : 0;
            totalDistance += (route.getMinimumRange() != null) ? route.getMinimumRange() : 0.0;

            // Se for escala, soma os 60 minutos do Minimum Connection Time
            if (i < path.size() - 1) {
                totalTime += MINIMUM_CONNECTING_TIME_MINUTES;
            }
        }

        return new AlternativeRouteDTO(legDTOs, totalTime, totalDistance);
    }
}