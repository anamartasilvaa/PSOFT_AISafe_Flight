package pt.isep.psoft.aisafe.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteHistoryDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateRouteDTO;
import pt.isep.psoft.aisafe.application.RouteService;

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
    public ResponseEntity<EntityModel<RouteViewDTO>> updateRoute(@PathVariable String id, @Valid @RequestBody UpdateRouteDTO dto) {
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

    // --- US111: Ver histórico de uma rota ---
    @GetMapping("/{id}/history")
    public ResponseEntity<CollectionModel<EntityModel<RouteHistoryDTO>>> getRouteHistory(@PathVariable String id) {
        List<EntityModel<RouteHistoryDTO>> history = routeService.getRouteHistory(id)
                .stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(RouteController.class).getRouteById(id)).withRel("route-details")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RouteHistoryDTO>> collectionModel = CollectionModel.of(history,
                linkTo(methodOn(RouteController.class).getRouteHistory(id)).withSelfRel());

        return ResponseEntity.ok().body(collectionModel);
    }

    // --- US113: Pesquisar Rotas por Aeroporto  ---
    @GetMapping("/airport/{iata}")
    @Operation(summary = "Search routes by origin airport (Paged)")
    public ResponseEntity<PagedModel<EntityModel<RouteViewDTO>>> searchRoutesByAirport(
            @PathVariable String iata,
            @PageableDefault(size = 10) Pageable pageable,
            PagedResourcesAssembler<RouteViewDTO> assembler) {

        Page<RouteViewDTO> page = routeService.searchRoutes(iata, null, pageable);
        PagedModel<EntityModel<RouteViewDTO>> pagedModel = assembler.toModel(page, this::addLinksToRoute);

        return ResponseEntity.ok(pagedModel);
    }

    // --- US113 e US114: Pesquisar Rotas  ---
    @GetMapping
    @Operation(summary = "Search routes by origin, destination, or both (Paged)")
    public ResponseEntity<PagedModel<EntityModel<RouteViewDTO>>> searchRoutes(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @PageableDefault(size = 10) Pageable pageable,
            PagedResourcesAssembler<RouteViewDTO> assembler) {

        Page<RouteViewDTO> page = routeService.searchRoutes(origin, destination, pageable);
        PagedModel<EntityModel<RouteViewDTO>> pagedModel = assembler.toModel(page, this::addLinksToRoute);

        return ResponseEntity.ok(pagedModel);
    }

    private EntityModel<RouteViewDTO> addLinksToRoute(RouteViewDTO route) {
        EntityModel<RouteViewDTO> em = EntityModel.of(route);
        em.add(linkTo(methodOn(RouteController.class).updateRoute(route.routeId(), null)).withRel("update-route"));
        em.add(linkTo(methodOn(RouteController.class).deactivateRoute(route.routeId())).withRel("deactivate-route"));
        em.add(linkTo(methodOn(RouteController.class).getRouteHistory(route.routeId())).withRel("route-history"));
        return em;
    }

    /* US209 - View all routes that depart from or arrive at a specific airport */
    @GetMapping("/involving/{iataCode}")
    @Operation(summary = "View all routes that depart from or arrive at a specific airport (Paged)")
    public ResponseEntity<PagedModel<EntityModel<RouteViewDTO>>> getRoutesInvolvingAirport(
            @PathVariable String iataCode,
            @PageableDefault(size = 10) Pageable pageable,
            PagedResourcesAssembler<RouteViewDTO> assembler) {
        Page<RouteViewDTO> page = routeService.getRoutesByAirport(iataCode, pageable);
        PagedModel<EntityModel<RouteViewDTO>> pagedModel = assembler.toModel(page, this::addLinksToRoute);

        return ResponseEntity.ok(pagedModel);
    }

    /* US210 - Generate statistics on the busiest airports by number of routes */
    @GetMapping("/statistics/busiest-airports")
    public ResponseEntity<CollectionModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO>>> getBusiestAirports() {

        List<pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO> stats = routeService.getBusiestAirports();

        if (stats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO>> resources = stats.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO>> collectionModel = CollectionModel.of(resources,
                linkTo(methodOn(RouteController.class).getBusiestAirports()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }
}