package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.RouteUtilizationDTO;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final ScheduledFlightRepository scheduledFlightRepository;

    public StatisticsService(ScheduledFlightRepository scheduledFlightRepository) {
        this.scheduledFlightRepository = scheduledFlightRepository;
    }

    public List<RouteUtilizationDTO> getMostFrequentRoutes() {
        List<Object[]> results = scheduledFlightRepository.countFlightsGroupedByRoute();

        return results.stream()
                .map(row -> new RouteUtilizationDTO(
                        (String) row[0], // routeId
                        (Long) row[1]    // flightCount
                ))
                .collect(Collectors.toList());
    }
}