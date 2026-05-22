package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    // US115a - Create Template (Manual)
    @PostMapping("/templates")
    public ResponseEntity<MaintenanceTemplate> createTemplate(@Valid @RequestBody CreateMaintenanceTemplateDTO dto) {
        MaintenanceTemplate created = maintenanceService.createTemplate(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // US115 - Register Active Maintenance Record
    @PostMapping("/records")
    public ResponseEntity<EntityModel<MaintenanceRecord>> registerMaintenance(@Valid @RequestBody RegisterMaintenanceDTO dto) {
        MaintenanceRecord record = maintenanceService.registerMaintenance(dto);

        EntityModel<MaintenanceRecord> resource = EntityModel.of(record);

        resource.add(linkTo(methodOn(MaintenanceController.class).completeMaintenance(record.getPk(), null)).withRel("complete-this-record"));

        resource.add(linkTo(methodOn(MaintenanceController.class).getAircraftHistory(dto.registrationNumber())).withRel("aircraft-history"));

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    // US116 - Get History by Aircraft Registration Number
    @GetMapping("/records/aircraft/{registrationNumber}")
    public ResponseEntity<List<MaintenanceRecord>> getAircraftHistory(@PathVariable String registrationNumber) {
        return ResponseEntity.ok(maintenanceService.getAircraftHistory(registrationNumber));
    }

    // US117 - Get Fleet Total Maintenance Hours
    @GetMapping("/records/total-hours")
    public ResponseEntity<Integer> getTotalHours() {
        return ResponseEntity.ok(maintenanceService.getTotalMaintenanceHours());
    }

    // US119 - Mark Maintenance as Completed
    @PatchMapping("/records/{id}/complete")
    public ResponseEntity<EntityModel<MaintenanceRecord>> completeMaintenance(
            @PathVariable Long id,
            @Valid @RequestBody CompleteMaintenanceDTO dto) {

        MaintenanceRecord record = maintenanceService.completeMaintenance(id, dto);


        EntityModel<MaintenanceRecord> resource = EntityModel.of(record);
        resource.add(linkTo(methodOn(MaintenanceController.class).getTotalHours()).withRel("view-fleet-total-hours"));

        return ResponseEntity.ok(resource);
    }
}