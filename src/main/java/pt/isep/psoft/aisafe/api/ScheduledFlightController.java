package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.application.DTO.ScheduledFlightViewDTO;
import pt.isep.psoft.aisafe.application.ScheduledFlightService;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/scheduled-flights")
public class ScheduledFlightController {

    private final ScheduledFlightService service;

    public ScheduledFlightController(ScheduledFlightService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EntityModel<ScheduledFlightViewDTO>> scheduleFlight(@Valid @RequestBody CreateScheduledFlightDTO dto) {
        ScheduledFlightViewDTO created = service.scheduleFlight(dto);
        EntityModel<ScheduledFlightViewDTO> resource = EntityModel.of(created);

        resource.add(linkTo(methodOn(ScheduledFlightController.class).getFlightsByAircraft(dto.registrationNumber())).withRel("aircraft-flights"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping("/aircraft/{registrationNumber}")
    public ResponseEntity<CollectionModel<EntityModel<ScheduledFlightViewDTO>>> getFlightsByAircraft(@PathVariable String registrationNumber) {
        var flights = service.getScheduledFlightsByAircraft(registrationNumber);
        var resources = flights.stream()
                .map(dto -> EntityModel.of(dto, linkTo(methodOn(ScheduledFlightController.class).getFlightsByAircraft(registrationNumber)).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(resources, linkTo(methodOn(ScheduledFlightController.class).getFlightsByAircraft(registrationNumber)).withSelfRel()));
    }
}