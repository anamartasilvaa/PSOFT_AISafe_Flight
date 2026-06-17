package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.application.DTO.ScheduledFlightViewDTO;
import pt.isep.psoft.aisafe.application.FlightService;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    // US212 - Assign an aircraft to a route (Create Scheduled Flight)
    @PostMapping
    public ResponseEntity<EntityModel<ScheduledFlightViewDTO>> scheduleFlight(@Valid @RequestBody CreateScheduledFlightDTO dto) {

        ScheduledFlightViewDTO createdFlight = flightService.scheduleFlight(dto);
        EntityModel<ScheduledFlightViewDTO> resource = EntityModel.of(createdFlight);

        // HATEOAS Links
        resource.add(linkTo(methodOn(FlightController.class).scheduleFlight(dto)).withSelfRel());
        resource.add(linkTo(methodOn(FlightController.class).getFlightsByAircraft(dto.registrationNumber())).withRel("aircraft-flights"));

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    // US213 - View all scheduled flights for a specific aircraft
    @GetMapping("/aircraft/{registrationNumber}")
    public ResponseEntity<CollectionModel<EntityModel<ScheduledFlightViewDTO>>> getFlightsByAircraft(@PathVariable String registrationNumber) {

        List<ScheduledFlightViewDTO> flights = flightService.getScheduledFlightsByAircraft(registrationNumber);

        List<EntityModel<ScheduledFlightViewDTO>> resources = flights.stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(FlightController.class).getFlightsByAircraft(registrationNumber)).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ScheduledFlightViewDTO>> collection = CollectionModel.of(resources,
                linkTo(methodOn(FlightController.class).getFlightsByAircraft(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(collection);
    }
}