package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;
import pt.isep.psoft.aisafe.domain.MaintenanceRecordStatus;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<EntityModel<MaintenanceTemplate>> createTemplate(@Valid @RequestBody CreateMaintenanceTemplateDTO dto) {
        MaintenanceTemplate created = maintenanceService.createTemplate(dto);
        EntityModel<MaintenanceTemplate> resource = EntityModel.of(created);
        resource.add(linkTo(methodOn(MaintenanceController.class).createTemplate(dto)).withSelfRel());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
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

    // US117 - Get Fleet Total Maintenance Hours
    // US117 - Get Fleet Total Maintenance Hours
    @GetMapping("/records/total-hours")
    public ResponseEntity<EntityModel<java.util.Map<String, Integer>>> getTotalHours() {
        Integer total = maintenanceService.getTotalMaintenanceHours();

        // Embrulha o número num objeto para o JSON poder receber os _links
        java.util.Map<String, Integer> responseBody = java.util.Map.of("totalHours", total != null ? total : 0);

        EntityModel<java.util.Map<String, Integer>> resource = EntityModel.of(responseBody);
        resource.add(linkTo(methodOn(MaintenanceController.class).getTotalHours()).withSelfRel());

        return ResponseEntity.ok(resource);
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


    @GetMapping("/records/search")
    public ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<MaintenanceRecord>>> searchRecords(
            @RequestParam(required = false) String registrationNumber,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String category,
            org.springframework.data.domain.Pageable pageable,
            org.springframework.data.web.PagedResourcesAssembler<MaintenanceRecord> assembler) { // <-- Caminho completo forçado aqui!

        org.springframework.data.domain.Page<MaintenanceRecord> page =
                maintenanceService.searchMaintenanceRecords(registrationNumber, fromDate, toDate, category, pageable);

        org.springframework.hateoas.PagedModel<EntityModel<MaintenanceRecord>> pagedModel = assembler.toModel(page, record -> {
            EntityModel<MaintenanceRecord> em = EntityModel.of(record);
            if (record.getStatus() == MaintenanceRecordStatus.SCHEDULED) {
                em.add(linkTo(methodOn(MaintenanceController.class).completeMaintenance(record.getPk(), null)).withRel("complete-this-record"));
            }
            return em;
        });

        return ResponseEntity.ok(pagedModel);
    }

    // US219 - View ongoing maintenance activities across the fleet
    @GetMapping("/records/ongoing")
    public ResponseEntity<CollectionModel<EntityModel<MaintenanceRecord>>> getOngoingMaintenances() {

        List<EntityModel<MaintenanceRecord>> records = maintenanceService.getOngoingMaintenances()
                .stream()
                .map(record -> {
                    EntityModel<MaintenanceRecord> em = EntityModel.of(record);
                    // Adiciona o link para poder concluir a manutenção diretamente daqui
                    em.add(linkTo(methodOn(MaintenanceController.class).completeMaintenance(record.getPk(), null)).withRel("complete-this-record"));
                    return em;
                })
                .collect(Collectors.toList());

        CollectionModel<EntityModel<MaintenanceRecord>> collection = CollectionModel.of(records);
        collection.add(linkTo(methodOn(MaintenanceController.class).getOngoingMaintenances()).withSelfRel());

        return ResponseEntity.ok(collection);
    }


}