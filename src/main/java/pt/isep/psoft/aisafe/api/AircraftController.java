package pt.isep.psoft.aisafe.api;

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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/aircraft-models")
public class AircraftController {

    private final AircraftService service;

    public AircraftController(AircraftService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EntityModel<RegisterAircraftModelDTO>> createModel(@RequestBody RegisterAircraftModelDTO dto) {
        service.registerModel(dto);
        EntityModel<RegisterAircraftModelDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).createModel(dto)).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @PostMapping("/instances")
    public ResponseEntity<EntityModel<RegisterAircraftDTO>> createAircraft(@RequestBody RegisterAircraftDTO dto) {
        service.registerAircraft(dto);

        EntityModel<RegisterAircraftDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).createAircraft(dto)).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @GetMapping("/instances/{registrationNumber}")
    public ResponseEntity<EntityModel<AircraftViewDTO>> getAircraft(@PathVariable String registrationNumber) {
        AircraftViewDTO dto = service.getAircraftByRegistrationNumber(registrationNumber);

        EntityModel<AircraftViewDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(AircraftController.class).getAircraft(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    // --- AQUI ESTÁ O GET ATUALIZADO (JÁ COM A CHAVETA FECHADA!) ---
    @GetMapping("/instances")
    public ResponseEntity<Page<AircraftViewDTO>> searchAircrafts(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<AircraftViewDTO> pagedList = service.searchAircrafts(model, status, page, size);
        return ResponseEntity.ok(pagedList);
    }

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