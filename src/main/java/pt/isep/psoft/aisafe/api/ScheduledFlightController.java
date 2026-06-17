package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.application.ScheduledFlightService;
import pt.isep.psoft.aisafe.domain.ScheduledFlight;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/scheduled-flights")
public class ScheduledFlightController {

    private final ScheduledFlightService scheduledFlightService;

    public ScheduledFlightController(ScheduledFlightService scheduledFlightService) {
        this.scheduledFlightService = scheduledFlightService;
    }

    // US212 - Assign an aircraft to a route for a specific date and time
    @PostMapping
    public ResponseEntity<EntityModel<ScheduledFlight>> scheduleFlight(@Valid @RequestBody CreateScheduledFlightDTO dto) {
        ScheduledFlight flight = scheduledFlightService.scheduleFlight(dto);

        EntityModel<ScheduledFlight> resource = EntityModel.of(flight);
        // Adiciona um link para o utilizador poder ver facilmente todos os voos deste avião
        resource.add(linkTo(methodOn(ScheduledFlightController.class).getScheduledFlightsByAircraft(dto.registrationNumber())).withRel("view-all-aircraft-flights"));

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    // US213 - View all scheduled flights for a specific aircraft
    @GetMapping("/aircraft/{registrationNumber}")
    public ResponseEntity<CollectionModel<EntityModel<ScheduledFlight>>> getScheduledFlightsByAircraft(@PathVariable String registrationNumber) {

        List<ScheduledFlight> rawFlights = scheduledFlightService.getScheduledFlightsByAircraft(registrationNumber);

        if (rawFlights.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<ScheduledFlight>> flights = rawFlights.stream()
                .map(EntityModel::of)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<ScheduledFlight>> collection = CollectionModel.of(flights);
        collection.add(linkTo(methodOn(ScheduledFlightController.class).getScheduledFlightsByAircraft(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(collection);
    }
}