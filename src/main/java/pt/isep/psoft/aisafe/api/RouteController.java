package pt.isep.psoft.aisafe.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateRouteDTO;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.domain.RouteHistory;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // --- US110: Criar uma Rota ---
    @PostMapping
    @Operation(summary = "Creates a new flight route")
    public ResponseEntity<EntityModel<RouteViewDTO>> createRoute(@Valid @RequestBody CreateRouteDTO dto) {

        RouteViewDTO created = routeService.createRoute(dto);
        EntityModel<RouteViewDTO> resource = EntityModel.of(created);

        String rId = created.routeId();

        resource.add(linkTo(methodOn(RouteController.class).updateRoute(rId, null)).withRel("update-route"));
        resource.add(linkTo(methodOn(RouteController.class).deactivateRoute(rId)).withRel("deactivate-route"));
        resource.add(linkTo(methodOn(RouteController.class).getRouteHistory(rId)).withRel("route-history"));

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    // --- US112: Atualizar Parâmetros da Rota ---
    @PatchMapping("/{id}")
    @Operation(summary = "Updates technical parameters of an existing route")
    public ResponseEntity<EntityModel<RouteViewDTO>> updateRoute(
            @PathVariable String id,
            @Valid @RequestBody UpdateRouteDTO dto) {

        RouteViewDTO updated = routeService.updateRoute(id, dto);
        EntityModel<RouteViewDTO> resource = EntityModel.of(updated);

        resource.add(linkTo(methodOn(RouteController.class).deactivateRoute(id)).withRel("deactivate-route"));
        resource.add(linkTo(methodOn(RouteController.class).getRouteHistory(id)).withRel("route-history"));

        return ResponseEntity.ok(resource);
    }

    // --- US112: Desativar a Rota ---
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivates a route")
    public ResponseEntity<EntityModel<RouteViewDTO>> deactivateRoute(@PathVariable String id) {

        RouteViewDTO deactivated = routeService.deactivateRoute(id);
        EntityModel<RouteViewDTO> resource = EntityModel.of(deactivated);

        resource.add(linkTo(methodOn(RouteController.class).updateRoute(id, null)).withRel("update-route"));
        resource.add(linkTo(methodOn(RouteController.class).getRouteHistory(id)).withRel("route-history"));

        return ResponseEntity.ok(resource);
    }

    // --- US113: Ver detalhes de uma rota pelo ID ---
    @GetMapping("/{id}")
    @Operation(summary = "Get route details by ID")
    public ResponseEntity<EntityModel<RouteViewDTO>> getRouteById(@PathVariable String id) {

        RouteViewDTO route = routeService.getRoute(id);
        EntityModel<RouteViewDTO> resource = EntityModel.of(route);

        resource.add(linkTo(methodOn(RouteController.class).getRouteById(id)).withSelfRel());
        resource.add(linkTo(methodOn(RouteController.class).updateRoute(id, null)).withRel("update-route"));
        resource.add(linkTo(methodOn(RouteController.class).getRouteHistory(id)).withRel("route-history"));

        return ResponseEntity.ok(resource);
    }

    // --- US111: Ver histórico de uma rota (NOVO) ---
    @GetMapping("/{id}/history")
    @Operation(summary = "Get history of a specific route")
    public ResponseEntity<List<RouteHistory>> getRouteHistory(@PathVariable String id) {
        return ResponseEntity.ok(routeService.getRouteHistory(id));
    }

    // --- US113: Pesquisar Rotas por Aeroporto ---
    @GetMapping("/airport/{iata}")
    @Operation(summary = "Search routes by origin airport")
    public ResponseEntity<CollectionModel<EntityModel<RouteViewDTO>>> searchRoutesByAirport(@PathVariable String iata) {

        List<EntityModel<RouteViewDTO>> routes = routeService.searchRoutes(iata, null)
                .stream()
                .map(route -> {
                    EntityModel<RouteViewDTO> em = EntityModel.of(route);
                    em.add(linkTo(methodOn(RouteController.class).updateRoute(route.routeId(), null)).withRel("update-route"));
                    em.add(linkTo(methodOn(RouteController.class).deactivateRoute(route.routeId())).withRel("deactivate-route"));
                    em.add(linkTo(methodOn(RouteController.class).getRouteHistory(route.routeId())).withRel("route-history"));
                    return em;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RouteViewDTO>> collection = CollectionModel.of(routes);
        collection.add(linkTo(methodOn(RouteController.class).searchRoutesByAirport(iata)).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    // --- US113 e US114: Pesquisar Rotas ---
    @GetMapping
    @Operation(summary = "Search routes by origin, destination, or both")
    public ResponseEntity<CollectionModel<EntityModel<RouteViewDTO>>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {

        List<EntityModel<RouteViewDTO>> routes = routeService.searchRoutes(origin, destination)
                .stream()
                .map(route -> {
                    EntityModel<RouteViewDTO> em = EntityModel.of(route);

                    em.add(linkTo(methodOn(RouteController.class).updateRoute(route.routeId(), null)).withRel("update-route"));
                    em.add(linkTo(methodOn(RouteController.class).deactivateRoute(route.routeId())).withRel("deactivate-route"));
                    em.add(linkTo(methodOn(RouteController.class).getRouteHistory(route.routeId())).withRel("route-history"));
                    return em;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RouteViewDTO>> collection = CollectionModel.of(routes);
        collection.add(linkTo(methodOn(RouteController.class).searchRoutes(origin, destination)).withSelfRel());

        return ResponseEntity.ok(collection);
    }
}