package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.domain.ComponentCategory;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;
import pt.isep.psoft.aisafe.domain.MaintenanceRecordStatus;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/maintenance")
@SuppressWarnings("NullableProblems")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping("/templates")
    public ResponseEntity<EntityModel<MaintenanceTemplate>> createTemplate(@Valid @RequestBody CreateMaintenanceTemplateDTO dto) {
        MaintenanceTemplate created = maintenanceService.createTemplate(dto);
        EntityModel<MaintenanceTemplate> resource = EntityModel.of(created);
        resource.add(linkTo(methodOn(MaintenanceController.class).createTemplate(dto)).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @PostMapping("/records")
    public ResponseEntity<EntityModel<MaintenanceRecord>> registerMaintenance(@Valid @RequestBody RegisterMaintenanceDTO dto) {
        MaintenanceRecord record = maintenanceService.registerMaintenance(dto);
        EntityModel<MaintenanceRecord> resource = EntityModel.of(record);
        resource.add(linkTo(methodOn(MaintenanceController.class).completeMaintenance(record.getPk(), null)).withRel("complete-this-record"));
        resource.add(linkTo(methodOn(MaintenanceController.class).getAircraftHistory(dto.registrationNumber())).withRel("aircraft-history"));
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @GetMapping("/records/aircraft/{registrationNumber}")
    public ResponseEntity<CollectionModel<EntityModel<MaintenanceRecord>>> getAircraftHistory(@PathVariable String registrationNumber) {
        List<EntityModel<MaintenanceRecord>> records = maintenanceService.getAircraftHistory(registrationNumber)
                .stream()
                .map(record -> {
                    EntityModel<MaintenanceRecord> em = EntityModel.of(record);
                    if (record.getStatus() == MaintenanceRecordStatus.SCHEDULED) {
                        em.add(linkTo(methodOn(MaintenanceController.class).completeMaintenance(record.getPk(), null)).withRel("complete-this-record"));
                    }
                    return em;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<MaintenanceRecord>> collection = CollectionModel.of(records);
        collection.add(linkTo(methodOn(MaintenanceController.class).getAircraftHistory(registrationNumber)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/records/total-hours")
    public ResponseEntity<EntityModel<java.util.Map<String, Integer>>> getTotalHours() {
        Integer total = maintenanceService.getTotalMaintenanceHours();
        java.util.Map<String, Integer> responseBody = java.util.Map.of("totalHours", total != null ? total : 0);

        EntityModel<java.util.Map<String, Integer>> resource = EntityModel.of(responseBody);
        resource.add(linkTo(methodOn(MaintenanceController.class).getTotalHours()).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/records/aircraft/{registrationNumber}/total-hours")
    public ResponseEntity<EntityModel<java.util.Map<String, Object>>> getTotalHoursByAircraft(@PathVariable String registrationNumber) {
        Integer total = maintenanceService.getTotalMaintenanceHoursByAircraft(registrationNumber);
        java.util.Map<String, Object> responseBody = new java.util.HashMap<>();
        responseBody.put("registrationNumber", registrationNumber);
        responseBody.put("totalHours", total != null ? total : 0);

        EntityModel<java.util.Map<String, Object>> resource = EntityModel.of(responseBody);
        resource.add(linkTo(methodOn(MaintenanceController.class).getTotalHoursByAircraft(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(resource);
    }

    @PatchMapping("/records/{id}/complete")
    public ResponseEntity<EntityModel<MaintenanceRecord>> completeMaintenance(@PathVariable Long id, @Valid @RequestBody CompleteMaintenanceDTO dto) {
        MaintenanceRecord record = maintenanceService.completeMaintenance(id, dto);
        EntityModel<MaintenanceRecord> resource = EntityModel.of(record);
        resource.add(linkTo(methodOn(MaintenanceController.class).getTotalHours()).withRel("view-fleet-total-hours"));
        return ResponseEntity.ok(resource);
    }

    @GetMapping("/records/search")
    public ResponseEntity<PagedModel<EntityModel<MaintenanceRecord>>> searchRecords(
            @RequestParam(required = false) String registrationNumber,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false, name = "componentCategory") String category,
            Pageable pageable,
            PagedResourcesAssembler<MaintenanceRecord> assembler) {

        ComponentCategory categoryEnum = null;
        if (category != null && !category.isBlank()) {
            try {
                categoryEnum = ComponentCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Page<MaintenanceRecord> page = maintenanceService.searchMaintenanceRecords(registrationNumber, fromDate, toDate, categoryEnum, pageable);

        PagedModel<EntityModel<MaintenanceRecord>> pagedModel = assembler.toModel(page, record -> {
            EntityModel<MaintenanceRecord> em = EntityModel.of(record);
            if (record.getStatus() == MaintenanceRecordStatus.SCHEDULED) {
                em.add(linkTo(methodOn(MaintenanceController.class).completeMaintenance(record.getPk(), null)).withRel("complete-this-record"));
            }
            return em;
        });

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/records/ongoing")
    public ResponseEntity<CollectionModel<EntityModel<MaintenanceRecord>>> getOngoingMaintenances() {
        List<EntityModel<MaintenanceRecord>> records = maintenanceService.getOngoingMaintenances()
                .stream()
                .map(record -> {
                    EntityModel<MaintenanceRecord> em = EntityModel.of(record);
                    em.add(linkTo(methodOn(MaintenanceController.class).completeMaintenance(record.getPk(), null)).withRel("complete-this-record"));
                    return em;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<MaintenanceRecord>> collection = CollectionModel.of(records);
        collection.add(linkTo(methodOn(MaintenanceController.class).getOngoingMaintenances()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/statistics/costs")
    public ResponseEntity<CollectionModel<EntityModel<MaintenanceCostDTO>>> getMaintenanceCosts(@RequestParam(required = false) String groupBy) {
        List<MaintenanceCostDTO> costs = maintenanceService.getMaintenanceCosts(groupBy);
        if (costs.isEmpty()) return ResponseEntity.noContent().build();
        List<EntityModel<MaintenanceCostDTO>> resources = costs.stream().map(EntityModel::of).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources, linkTo(methodOn(MaintenanceController.class).getMaintenanceCosts(groupBy)).withSelfRel()));
    }

    @GetMapping("/statistics/turnaround")
    public ResponseEntity<CollectionModel<EntityModel<TurnaroundTimeDTO>>> getTurnaroundTime() {
        List<TurnaroundTimeDTO> turnaroundStats = maintenanceService.getTurnaroundTimePerAircraftModel();
        if (turnaroundStats.isEmpty()) return ResponseEntity.noContent().build();
        List<EntityModel<TurnaroundTimeDTO>> resources = turnaroundStats.stream().map(EntityModel::of).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources, linkTo(methodOn(MaintenanceController.class).getTurnaroundTime()).withSelfRel()));
    }

    @GetMapping("/alerts")
    public ResponseEntity<CollectionModel<EntityModel<MaintenanceAlertDTO>>> getMaintenanceAlerts(@RequestParam(required = false) String type) {
        List<MaintenanceAlertDTO> alerts = maintenanceService.generateMaintenanceAlerts();
        if (type != null && !type.isEmpty()) {
            alerts = alerts.stream().filter(a -> a.alertType().equalsIgnoreCase(type)).collect(Collectors.toList());
        }
        if (alerts.isEmpty()) return ResponseEntity.noContent().build();
        List<EntityModel<MaintenanceAlertDTO>> resources = alerts.stream().map(EntityModel::of).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources, linkTo(methodOn(MaintenanceController.class).getMaintenanceAlerts(type)).withSelfRel()));
    }

    @GetMapping("/parts/low-stock-alerts")
    public ResponseEntity<CollectionModel<EntityModel<LowStockAlertDTO>>> getLowStockAlerts() {
        List<LowStockAlertDTO> alerts = maintenanceService.generateLowStockAlerts();
        if (alerts.isEmpty()) return ResponseEntity.noContent().build();
        List<EntityModel<LowStockAlertDTO>> resources = alerts.stream().map(EntityModel::of).collect(Collectors.toList());
        return ResponseEntity.ok(CollectionModel.of(resources, linkTo(methodOn(MaintenanceController.class).getLowStockAlerts()).withSelfRel()));
    }
}