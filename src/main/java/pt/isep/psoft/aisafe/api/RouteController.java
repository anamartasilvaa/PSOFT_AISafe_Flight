package pt.isep.psoft.aisafe.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ATCC')")
    @Operation(summary = "Creates a new flight route")
    public ResponseEntity<EntityModel<RouteViewDTO>> createRoute(@Valid @RequestBody CreateRouteDTO dto) {

        RouteViewDTO created = routeService.createRoute(dto);
        EntityModel<RouteViewDTO> resource = EntityModel.of(created);

        resource.add(linkTo(methodOn(RouteController.class).createRoute(dto)).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    // --- US112: Atualizar Parâmetros da Rota ---
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ATCC')")
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
    @PreAuthorize("hasAuthority('ATCC')")
    @Operation(summary = "Deactivates a route")
    public ResponseEntity<EntityModel<RouteViewDTO>> deactivateRoute(@PathVariable String id) {

        RouteViewDTO deactivated = routeService.deactivateRoute(id);
        EntityModel<RouteViewDTO> resource = EntityModel.of(deactivated);

        resource.add(linkTo(methodOn(RouteController.class).deactivateRoute(id)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    // --- US113 e US114: Pesquisar Rotas ---
    @GetMapping
    @PreAuthorize("hasAuthority('ATCC')")
    @Operation(summary = "Search routes by origin, destination, or both")
    public ResponseEntity<List<RouteViewDTO>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination) {


        List<RouteViewDTO> routes = routeService.searchRoutes(origin, destination);

        return ResponseEntity.ok(routes);
    }
}