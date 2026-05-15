package pt.isep.psoft.aisafe.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.aisafe.application.DTO.CreateMaintenanceTemplateDTO;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    // US115 - Criar um Template de Manutenção (Manual)
    @PostMapping("/templates")
    public ResponseEntity<MaintenanceTemplate> createTemplate(@RequestBody CreateMaintenanceTemplateDTO dto) {
        MaintenanceTemplate created = maintenanceService.createTemplate(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}