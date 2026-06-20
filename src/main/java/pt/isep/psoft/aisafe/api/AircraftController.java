package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/aircraft-models")
public class AircraftController {

    private final AircraftService service;

    public AircraftController(AircraftService service) {
        this.service = service;
    }

    /*US101 - Register an aircraft model*/
    @PostMapping
    public ResponseEntity<EntityModel<RegisterAircraftModelDTO>> createModel( @Valid @RequestBody RegisterAircraftModelDTO dto) {
        service.registerModel(dto);
        EntityModel<RegisterAircraftModelDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).createModel(dto)).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /*US102 - Register a specific aircraft instance*/
    @PostMapping("/instances")
    public ResponseEntity<EntityModel<RegisterAircraftDTO>> createAircraft(@Valid @RequestBody RegisterAircraftDTO dto) {
        service.registerAircraft(dto);
        EntityModel<RegisterAircraftDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).createAircraft(dto)).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /*US103 - View aircraft details by registration number*/
    @GetMapping("/instances/{registrationNumber}")
    public ResponseEntity<EntityModel<AircraftViewDTO>> getAircraft(@PathVariable String registrationNumber) {
        AircraftViewDTO dto = service.getAircraftByRegistrationNumber(registrationNumber);
        EntityModel<AircraftViewDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).getAircraft(registrationNumber)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    /*US104 - Search aircrafts*/
    @GetMapping("/instances")
    public ResponseEntity<CollectionModel<EntityModel<AircraftViewDTO>>> searchAircrafts(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year) {

        List<AircraftViewDTO> list = service.searchAircrafts(model, status, year);
        List<EntityModel<AircraftViewDTO>> resources = list.stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(AircraftController.class).getAircraft(dto.registrationNumber())).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(AircraftController.class).searchAircrafts(model, status, year)).withSelfRel()));
    }

    /*US105 + US222/US119 - Update an aircraft operational status & Trigger Swap Algorithm*/
    @PatchMapping("/instances/{registrationNumber}/status")
    public ResponseEntity<EntityModel<java.util.Map<String, Object>>> updateStatus(
            @PathVariable String registrationNumber,
            @Valid @RequestBody UpdateAircraftStatusDTO dto) {

        // Chamamos o novo método que devolve o Relatório do Algoritmo junto com o Avião
        java.util.Map<String, Object> responseMap = service.updateAircraftStatusWithReport(registrationNumber, dto);

        EntityModel<java.util.Map<String, Object>> resource = EntityModel.of(responseMap);
        resource.add(linkTo(methodOn(AircraftController.class).getAircraft(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US201  */
    @PatchMapping("/models/{modelName}/specifications")
    public ResponseEntity<EntityModel<AircraftModelViewDTO>> updateModelSpecifications(
            @PathVariable String modelName,
            @Valid @RequestBody UpdateAircraftModelSpecsDTO dto) {

        AircraftModelViewDTO updatedModel = service.updateModelSpecifications(modelName, dto);
        EntityModel<AircraftModelViewDTO> resource = EntityModel.of(updatedModel);
        resource.add(linkTo(methodOn(AircraftController.class)
                .updateModelSpecifications(modelName, dto)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US202 -*/
    @PatchMapping(value = "/models/{modelName}/image", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntityModel<AircraftModelViewDTO>> updateModelImage(
            @PathVariable String modelName,
            @RequestPart("image") MultipartFile file) {
        AircraftModelViewDTO updatedModel = service.updateModelImage(modelName, file);

        EntityModel<AircraftModelViewDTO> resource = EntityModel.of(updatedModel);

        resource.add(linkTo(methodOn(AircraftController.class).updateModelImage(modelName, null)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US204 */
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Top 5 Aircraft Models", description = "Get the top 5 most utilized aircraft models based on total flight hours or number of assignments.")
    @GetMapping("/models/top5")
    public ResponseEntity<CollectionModel<EntityModel<TopAircraftModelDTO>>> getTop5Models(
            @RequestParam(defaultValue = "hours") String sortBy) {

        List<TopAircraftModelDTO> top5 = service.getTop5UtilizedModels(sortBy);

        List<EntityModel<TopAircraftModelDTO>> resources = top5.stream()
                .map(EntityModel::of)
                .toList();

        CollectionModel<EntityModel<TopAircraftModelDTO>> collection = CollectionModel.of(resources,
                linkTo(methodOn(AircraftController.class).getTop5Models(sortBy)).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    /* US206  */
    @GetMapping("/instances/{registrationNumber}/operational-hours")
    public ResponseEntity<EntityModel<OperationalHoursDTO>> getOperationalHours(
            @PathVariable String registrationNumber) {

        OperationalHoursDTO dto = service.getAircraftOperationalHours(registrationNumber);

        EntityModel<OperationalHoursDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).getOperationalHours(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US206  */
    @GetMapping("/instances/operational-hours")
    public ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<OperationalHoursDTO>>> getAllOperationalHours(
            org.springframework.data.domain.Pageable pageable,
            org.springframework.data.web.PagedResourcesAssembler<OperationalHoursDTO> assembler) {

        org.springframework.data.domain.Page<OperationalHoursDTO> page = service.getAllAircraftOperationalHours(pageable);

        org.springframework.hateoas.PagedModel<EntityModel<OperationalHoursDTO>> pagedModel =
                assembler.toModel(page, dto -> EntityModel.of(dto,
                        linkTo(methodOn(AircraftController.class).getOperationalHours(dto.registrationNumber())).withSelfRel()));

        return ResponseEntity.ok(pagedModel);
    }

    /* US203*/
    @GetMapping("/instances/{registrationNumber}/compatible-routes")
    public ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<RouteViewDTO>>> getCompatibleRoutes(
            @PathVariable String registrationNumber,
            org.springframework.data.domain.Pageable pageable,
            org.springframework.data.web.PagedResourcesAssembler<RouteViewDTO> assembler) {

        org.springframework.data.domain.Page<RouteViewDTO> compatibleRoutes =
                service.getCompatibleRoutesForAircraft(registrationNumber, pageable);
        org.springframework.hateoas.PagedModel<EntityModel<RouteViewDTO>> pagedModel =
                assembler.toModel(compatibleRoutes, dto -> EntityModel.of(dto));

        return ResponseEntity.ok(pagedModel);
    }

    // US205
    @GetMapping("/instances/{registrationNumber}/real-time-status")
    public ResponseEntity<EntityModel<RealTimeStatusDTO>> getRealTimeStatus(@PathVariable String registrationNumber) {

        String status = service.getRealTimeAircraftStatus(registrationNumber);

        RealTimeStatusDTO responseDto = new RealTimeStatusDTO(registrationNumber, status);

        // HATEOAS
        EntityModel<RealTimeStatusDTO> resource = EntityModel.of(responseDto);
        resource.add(linkTo(methodOn(AircraftController.class).getRealTimeStatus(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US224 - Search for aircraft by specific features (e.g., WiFi, engine type) */
    @GetMapping("/instances/search-features")
    public ResponseEntity<CollectionModel<EntityModel<AircraftViewDTO>>> searchAircraftByFeatures(
            @RequestParam(required = false) String feature,
            @RequestParam(required = false) String engineType) {

        List<AircraftViewDTO> list = service.searchAircraftByFeatures(feature, engineType);

        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<AircraftViewDTO>> resources = list.stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(AircraftController.class).getAircraft(dto.registrationNumber())).withSelfRel()))
                .toList();

        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(AircraftController.class).searchAircraftByFeatures(feature, engineType)).withSelfRel()));
    }
}