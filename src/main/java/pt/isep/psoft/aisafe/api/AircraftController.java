package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    /*US105 - Update an aircraft operational status*/
    @PatchMapping("/instances/{registrationNumber}/status")
    public ResponseEntity<EntityModel<AircraftViewDTO>> updateStatus(
            @PathVariable String registrationNumber,
            @Valid @RequestBody UpdateAircraftStatusDTO dto) {

        AircraftViewDTO updatedAircraft = service.updateAircraftStatus(registrationNumber, dto);
        EntityModel<AircraftViewDTO> resource = EntityModel.of(updatedAircraft);
        resource.add(linkTo(methodOn(AircraftController.class).getAircraft(registrationNumber)).withSelfRel());
        return ResponseEntity.ok(resource);
    }


    /* US201 - Update an aircraft model's specifications */
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

    /* US202 - Register an aircraft model with an optional image or technical diagram */
    @PatchMapping("/models/{modelName}/image")
    public ResponseEntity<EntityModel<AircraftModelViewDTO>> updateModelImage(
            @PathVariable String modelName,
            @Valid @RequestBody UpdateAircraftModelImageDTO dto) {

        AircraftModelViewDTO updatedModel = service.updateModelImage(modelName, dto.imageUrl());

        // Adicionado o HATEOAS
        EntityModel<AircraftModelViewDTO> resource = EntityModel.of(updatedModel);
        resource.add(linkTo(methodOn(AircraftController.class).updateModelImage(modelName, dto)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US204 - Top 5 most utilized aircraft models based on total flight hours */
    @GetMapping("/models/top5")
    public ResponseEntity<CollectionModel<EntityModel<TopAircraftModelDTO>>> getTop5Models() {

        List<TopAircraftModelDTO> top5 = service.getTop5UtilizedModels();

        // 1. Embrulhar cada item da lista num EntityModel (podemos adicionar um link para o próprio modelo)
        List<EntityModel<TopAircraftModelDTO>> resources = top5.stream()
                .map(dto -> EntityModel.of(dto,
                        // Link fictício para o detalhe do modelo, se o tivéssemos
                        linkTo(methodOn(AircraftController.class).getTop5Models()).withRel("model-details")))
                .toList();

        // 2. Embrulhar a lista toda num CollectionModel com o link "self" para a própria pesquisa
        CollectionModel<EntityModel<TopAircraftModelDTO>> collection = CollectionModel.of(resources,
                linkTo(methodOn(AircraftController.class).getTop5Models()).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    /* US206 - Calculate the total operational hours for each aircraft */
    @GetMapping("/instances/{registrationNumber}/operational-hours")
    public ResponseEntity<EntityModel<OperationalHoursDTO>> getOperationalHours(
            @PathVariable String registrationNumber) {

        OperationalHoursDTO dto = service.getAircraftOperationalHours(registrationNumber);

        // Adicionado o HATEOAS
        EntityModel<OperationalHoursDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).getOperationalHours(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US203 - View compatible routes for a specific aircraft */
    @GetMapping("/instances/{registrationNumber}/compatible-routes")
    public ResponseEntity<CollectionModel<EntityModel<RouteViewDTO>>> getCompatibleRoutes(
            @PathVariable String registrationNumber) {

        List<RouteViewDTO> compatibleRoutes = service.getCompatibleRoutesForAircraft(registrationNumber);

        // 1. Embrulhar cada DTO num EntityModel
        List<EntityModel<RouteViewDTO>> resources = compatibleRoutes.stream()
                .map(EntityModel::of)
                .toList();

        // 2. Embrulhar a coleção e adicionar o self-link
        CollectionModel<EntityModel<RouteViewDTO>> collection = CollectionModel.of(resources,
                linkTo(methodOn(AircraftController.class).getCompatibleRoutes(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(collection);
    }

    /* US205 - View real-time aircraft availability status */
    @GetMapping("/instances/{registrationNumber}/real-time-status")
    public ResponseEntity<EntityModel<java.util.Map<String, String>>> getRealTimeStatus(@PathVariable String registrationNumber) {
        String status = service.getRealTimeAircraftStatus(registrationNumber);

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("registrationNumber", registrationNumber);
        response.put("realTimeStatus", status);

        // Adicionado o HATEOAS
        EntityModel<java.util.Map<String, String>> resource = EntityModel.of(response);
        resource.add(linkTo(methodOn(AircraftController.class).getRealTimeStatus(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(resource);
    }
}