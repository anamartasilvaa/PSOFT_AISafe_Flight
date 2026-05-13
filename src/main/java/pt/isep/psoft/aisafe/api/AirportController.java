package pt.isep.psoft.aisafe.api;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.AirportService;
import pt.isep.psoft.aisafe.application.DTO.AddCertificationDTO;
import pt.isep.psoft.aisafe.application.DTO.AirportViewDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAirportDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/airports")
public class AirportController {

    private final AirportService service;

    public AirportController(AirportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EntityModel<AirportViewDTO>> createAirport(@RequestBody RegisterAirportDTO dto) {
        AirportViewDTO created = service.registerAirport(dto);

        EntityModel<AirportViewDTO> resource = EntityModel.of(created);
        // Link para o GET do aeroporto criado
        resource.add(linkTo(methodOn(AirportController.class).getAirport(created.iataCode())).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @GetMapping("/{iataCode}")
    public ResponseEntity<EntityModel<AirportViewDTO>> getAirport(@PathVariable String iataCode) {
        AirportViewDTO airport = service.getAirportByIataCode(iataCode.toUpperCase());

        EntityModel<AirportViewDTO> resource = EntityModel.of(airport);

        // Link principal (Self)
        resource.add(linkTo(methodOn(AirportController.class).getAirport(iataCode)).withSelfRel());

        // Link de conveniência para adicionar certificações (US106a)
        resource.add(linkTo(methodOn(AirportController.class)
                .addCertification(iataCode, null)).withRel("add-certification"));

        return ResponseEntity.ok(resource);
    }

    @PostMapping("/{iataCode}/certifications")
    public ResponseEntity<EntityModel<AirportViewDTO>> addCertification(
            @PathVariable String iataCode,
            @RequestBody AddCertificationDTO dto) {

        AirportViewDTO updatedAirport = service.addCertification(iataCode, dto);

        EntityModel<AirportViewDTO> resource = EntityModel.of(updatedAirport);
        resource.add(linkTo(methodOn(AirportController.class).getAirport(iataCode)).withRel("airport-details"));
        resource.add(linkTo(methodOn(AirportController.class).addCertification(iataCode, dto)).withSelfRel());

        return ResponseEntity.ok(resource);
    }
}