package pt.isep.psoft.aisafe.api; // Ajusta o package conforme a tua estrutura

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isep.psoft.aisafe.application.StatisticsService;
import pt.isep.psoft.aisafe.application.DTO.RouteUtilizationDTO;

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
    // @RolesAllowed("BACKOFFICE") // Descomenta se tiveres a segurança ativada
    public ResponseEntity<CollectionModel<EntityModel<RouteUtilizationDTO>>> getMostFrequentRoutes() {

        List<RouteUtilizationDTO> frequentRoutes = statisticsService.getMostFrequentRoutes();

        if (frequentRoutes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<RouteUtilizationDTO>> models = frequentRoutes.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RouteUtilizationDTO>> collectionModel = CollectionModel.of(models);

        return ResponseEntity.ok(collectionModel);
    }
}