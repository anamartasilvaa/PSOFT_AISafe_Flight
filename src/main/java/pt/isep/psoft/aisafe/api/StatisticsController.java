package pt.isep.psoft.aisafe.api;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.StatisticsService;
import pt.isep.psoft.aisafe.application.DTO.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/routes/most-flown")
    public ResponseEntity<CollectionModel<EntityModel<RouteUtilizationDTO>>> getMostFrequentRoutes() {
        List<RouteUtilizationDTO> frequentRoutes = statisticsService.getMostFrequentRoutes();
        if (frequentRoutes.isEmpty()) return ResponseEntity.noContent().build();
        List<EntityModel<RouteUtilizationDTO>> models = frequentRoutes.stream().map(EntityModel::of).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models));
    }

    @GetMapping("/utilization-over-time")
    public ResponseEntity<CollectionModel<EntityModel<AircraftUtilizationRateDTO>>> getUtilizationOverTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<AircraftUtilizationRateDTO> data = statisticsService.getUtilizationRateOverTime(start, end);
        if (data.isEmpty()) return ResponseEntity.noContent().build();
        List<EntityModel<AircraftUtilizationRateDTO>> models = data.stream().map(EntityModel::of).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models));
    }

    @GetMapping("/fuel-efficiency")
    public ResponseEntity<CollectionModel<EntityModel<FuelEfficiencyDTO>>> getFuelEfficiency(
            @RequestParam(defaultValue = "true") boolean byAircraft) {
        List<FuelEfficiencyDTO> report = statisticsService.getFuelEfficiencyReport(byAircraft);
        if (report.isEmpty()) return ResponseEntity.noContent().build();
        List<EntityModel<FuelEfficiencyDTO>> models = report.stream().map(EntityModel::of).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(models));
    }
}