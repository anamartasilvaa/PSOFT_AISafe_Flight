package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.AirportService;
import pt.isep.psoft.aisafe.application.DTO.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        resource.add(linkTo(methodOn(AirportController.class).getAirport(created.iataCode())).withSelfRel());

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @GetMapping("/{iataCode}")
    public ResponseEntity<EntityModel<AirportViewDTO>> getAirport(@PathVariable String iataCode) {
        AirportViewDTO airport = service.getAirportByIataCode(iataCode.toUpperCase());

        EntityModel<AirportViewDTO> resource = EntityModel.of(airport);

        resource.add(linkTo(methodOn(AirportController.class).getAirport(iataCode)).withSelfRel());

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

    @GetMapping("/search")
    public ResponseEntity<CollectionModel<EntityModel<AirportViewDTO>>> searchAirports(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country) {

        List<AirportViewDTO> airports = service.searchAirports(name, city, country);

        List<EntityModel<AirportViewDTO>> airportResources = airports.stream()
                .map(dto -> EntityModel.of(dto,
                        linkTo(methodOn(AirportController.class).getAirport(dto.iataCode())).withSelfRel()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(airportResources,
                linkTo(methodOn(AirportController.class).searchAirports(name, city, country)).withSelfRel()));
    }
    @PatchMapping("/{iataCode}/status")
    public ResponseEntity<EntityModel<AirportViewDTO>> updateStatus(
            @PathVariable String iataCode,
            @RequestBody String newStatus) {

        AirportViewDTO updated = service.updateAirportStatus(iataCode, newStatus.replace("\"", ""));

        EntityModel<AirportViewDTO> resource = EntityModel.of(updated);
        resource.add(linkTo(methodOn(AirportController.class).getAirport(iataCode)).withRel("airport-details"));

        return ResponseEntity.ok(resource);
    }

    /* US207 - Upload an image or map of the airport */
    @PatchMapping("/{iataCode}/image")
    public ResponseEntity<EntityModel<AirportViewDTO>> updateAirportImage(
            @PathVariable String iataCode,
            @Valid @RequestBody UpdateAirportImageDTO dto) {
        AirportViewDTO updatedAirport = service.updateAirportImage(iataCode, dto.imageUrl());

        EntityModel<AirportViewDTO> resource = EntityModel.of(updatedAirport);

        resource.add(linkTo(methodOn(AirportController.class)
                .updateAirportImage(iataCode, dto)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US208 - Update airport details (operational hours and contact) */
    @PatchMapping("/{iataCode}/details")
    public ResponseEntity<EntityModel<AirportViewDTO>> updateAirportDetails(
            @PathVariable String iataCode,
            @Valid @RequestBody UpdateAirportDetailsDTO dto) {

        AirportViewDTO updatedAirport = service.updateAirportDetails(iataCode, dto);

        EntityModel<AirportViewDTO> resource = EntityModel.of(updatedAirport);
        resource.add(linkTo(methodOn(AirportController.class).getAirport(iataCode)).withRel("airport-details"));
        resource.add(linkTo(methodOn(AirportController.class).updateAirportDetails(iataCode, dto)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    /* US211 - View airports grouped by region, country, or both */
    @GetMapping("/grouped")
    public ResponseEntity<Object> getAirportsGrouped(
            @RequestParam(defaultValue = "country") String by) {

        Object groupedAirports = service.getAirportsGrouped(by);

        if (((java.util.Map<?, ?>) groupedAirports).isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(groupedAirports);
    }
}