package pt.isep.psoft.aisafe.api;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftModelDTO;
import pt.isep.psoft.aisafe.application.DTO.AircraftViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAircraftStatusDTO;
import org.springframework.data.domain.Page;

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

    /**
     * US101 - Register an aircraft model
     */
    @PostMapping
    public ResponseEntity<EntityModel<RegisterAircraftModelDTO>> createModel(@RequestBody RegisterAircraftModelDTO dto) {
        service.registerModel(dto);
        EntityModel<RegisterAircraftModelDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).createModel(dto)).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * US102 - Register a specific aircraft instance
     */
    @PostMapping("/instances")
    public ResponseEntity<EntityModel<RegisterAircraftDTO>> createAircraft(@RequestBody RegisterAircraftDTO dto) {
        service.registerAircraft(dto);
        EntityModel<RegisterAircraftDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).createAircraft(dto)).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    /**
     * US103 - View aircraft details by registration number
     */
    @GetMapping("/instances/{registrationNumber}")
    public ResponseEntity<EntityModel<AircraftViewDTO>> getAircraft(@PathVariable String registrationNumber) {
        AircraftViewDTO dto = service.getAircraftByRegistrationNumber(registrationNumber);
        EntityModel<AircraftViewDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).getAircraft(registrationNumber)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

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

    /**
     * US105 - Update an aircraft operational status
     */
    @PatchMapping("/instances/{registrationNumber}/status")
    public ResponseEntity<EntityModel<AircraftViewDTO>> updateStatus(
            @PathVariable String registrationNumber,
            @RequestBody UpdateAircraftStatusDTO dto) {

        AircraftViewDTO updatedAircraft = service.updateAircraftStatus(registrationNumber, dto);
        EntityModel<AircraftViewDTO> resource = EntityModel.of(updatedAircraft);
        resource.add(linkTo(methodOn(AircraftController.class).getAircraft(registrationNumber)).withSelfRel());
        return ResponseEntity.ok(resource);
    }
}