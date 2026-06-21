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

import java.util.List;
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

        resource.add(linkTo(methodOn(ScheduledFlightController.class)
                .getFlightsByAircraft(dto.registrationNumber()))
                .withRel("aircraft-flights"));

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping("/aircraft/{registrationNumber}")
    public ResponseEntity<CollectionModel<EntityModel<ScheduledFlightViewDTO>>> getFlightsByAircraft(@PathVariable String registrationNumber) {
        List<ScheduledFlightViewDTO> flights = service.getScheduledFlightsByAircraft(registrationNumber);

        // Retorna 204 se a lista estiver vazia
        if (flights.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<ScheduledFlightViewDTO>> resources = flights.stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(ScheduledFlightController.class).getFlightsByAircraft(registrationNumber)).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(resources,
                linkTo(methodOn(ScheduledFlightController.class).getFlightsByAircraft(registrationNumber)).withSelfRel()));
    }

    /* US229 - As a Backoffice Operator, I want to generate flight utilization reports showing which routes are most frequently flown. */
    @GetMapping("/route-utilization")
    public ResponseEntity<?> getRouteUtilizationReport() {

        var report = service.getRouteUtilizationReport();

        if (report == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(report);
    }
}