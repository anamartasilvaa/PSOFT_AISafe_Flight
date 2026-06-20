package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.DTO.RouteUtilizationDTO;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final ScheduledFlightRepository flightRepository;

    // Injeção de dependência via construtor (prática recomendada)
    public StatisticsService(ScheduledFlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public List<RouteUtilizationDTO> getMostFrequentRoutes() {
        return flightRepository.countFlightsGroupedByRoute().stream()
                .map(obj -> new RouteUtilizationDTO(
                        (String) obj[0],
                        (Long) obj[1]
                ))
                .collect(Collectors.toList());
    }
}