package pt.isep.psoft.aisafe.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    // US115 (Part 1) - Create Template (Manual)
    @PostMapping("/templates")
    public ResponseEntity<MaintenanceTemplate> createTemplate(@RequestBody CreateMaintenanceTemplateDTO dto) {
        MaintenanceTemplate created = maintenanceService.createTemplate(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // US115 (Part 2) - Register Active Maintenance Record
    @PostMapping("/records")
    public ResponseEntity<MaintenanceRecord> registerMaintenance(@RequestBody RegisterMaintenanceDTO dto) {
        MaintenanceRecord record = maintenanceService.registerMaintenance(dto);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
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
    public ResponseEntity<MaintenanceRecord> completeMaintenance(
            @PathVariable Long id,
            @RequestBody CompleteMaintenanceDTO dto) {
        return ResponseEntity.ok(maintenanceService.completeMaintenance(id, dto));
    }
}