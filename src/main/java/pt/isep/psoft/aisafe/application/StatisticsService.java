package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final ScheduledFlightRepository flightRepository;

    public StatisticsService(ScheduledFlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public List<RouteUtilizationDTO> getMostFrequentRoutes() {
        return flightRepository.countFlightsGroupedByRoute().stream()
                .map(obj -> new RouteUtilizationDTO((String) obj[0], (Long) obj[1]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AircraftUtilizationRateDTO> getUtilizationRateOverTime(LocalDateTime start, LocalDateTime end) {
        return flightRepository.countFlightsByMonth(start, end).stream()
                .map(obj -> new AircraftUtilizationRateDTO((String) obj[0], (Long) obj[1]))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FuelEfficiencyDTO> getFuelEfficiencyReport(boolean byAircraft) {
        List<Object[]> results = byAircraft ?
                flightRepository.getFuelEfficiencyPerAircraft() :
                flightRepository.getFuelEfficiencyPerRoute();

        return results.stream()
                .map(obj -> new FuelEfficiencyDTO((String) obj[0], (Double) obj[1]))
                .collect(Collectors.toList());
    }
}