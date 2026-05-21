package pt.isep.psoft.aisafe.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateRouteDTO;
import pt.isep.psoft.aisafe.application.RouteService;

import java.util.List;

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

        resource.add(linkTo(methodOn(RouteController.class).createRoute(dto)).withSelfRel());

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

        resource.add(linkTo(methodOn(RouteController.class).updateRoute(id, dto)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    // --- US112: Desativar a Rota ---
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivates a route")
    public ResponseEntity<EntityModel<RouteViewDTO>> deactivateRoute(@PathVariable String id) {

        RouteViewDTO deactivated = routeService.deactivateRoute(id);
        EntityModel<RouteViewDTO> resource = EntityModel.of(deactivated);

        resource.add(linkTo(methodOn(RouteController.class).deactivateRoute(id)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    // --- US113: Pesquisar Rotas por Aeroporto ---
    @GetMapping("/airport/{iata}")
    @Operation(summary = "Search routes by origin airport")
    public ResponseEntity<List<RouteViewDTO>> searchRoutesByAirport(@PathVariable String iata) {

        // Passamos o 'iata' (ex: OPO) como origem, e null como destino
        List<RouteViewDTO> routes = routeService.searchRoutes(iata, null);

        return ResponseEntity.ok(routes);
    }

    // --- US113 e US114: Pesquisar Rotas  ---
    @GetMapping
    @Operation(summary = "Search routes by origin, destination, or both")
    public ResponseEntity<List<RouteViewDTO>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {

        List<RouteViewDTO> routes = routeService.searchRoutes(origin, destination);

        return ResponseEntity.ok(routes);
    }
}