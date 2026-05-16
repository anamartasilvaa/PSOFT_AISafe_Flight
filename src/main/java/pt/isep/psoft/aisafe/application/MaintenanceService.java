package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.*;

import java.util.List;

@Service
public class MaintenanceService {

    private final MaintenanceTemplateRepository templateRepository;
    private final MaintenanceRecordRepository recordRepository;
    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository aircraftModelRepository; // Adicionado para procurar os modelos

    public MaintenanceService(MaintenanceTemplateRepository templateRepository,
                              MaintenanceRecordRepository recordRepository,
                              AircraftRepository aircraftRepository,
                              AircraftModelRepository aircraftModelRepository) {
        this.templateRepository = templateRepository;
        this.recordRepository = recordRepository;
        this.aircraftRepository = aircraftRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    // US115 (Part 1) - Create Maintenance Template
    public MaintenanceTemplate createTemplate(CreateMaintenanceTemplateDTO dto) {

        if (templateRepository.findByTemplateName(dto.templateName()).isPresent()) {
            throw new IllegalArgumentException("Template name already exists: " + dto.templateName());
        }


        List<AircraftModel> models = (List<AircraftModel>) aircraftModelRepository.findAllById(dto.applicableModelIds());


        List<ChecklistItem> items = dto.checklistItems().stream()
                .map(itemDto -> new ChecklistItem(itemDto.taskDescription(), itemDto.isMandatory()))
                .toList();

        Checklist checklist = new Checklist(dto.checklistTitle(), dto.checklistVersion(), items);


        MaintenanceTemplate template = new MaintenanceTemplate(
                dto.templateName(),
                dto.templateType(),
                dto.flightHoursInterval(),
                dto.calendarDaysInterval(),
                checklist,
                models
        );
        return templateRepository.save(template);
    }

    // US115 (Part 2) - Register Maintenance Event for an Aircraft
    public MaintenanceRecord registerMaintenance(RegisterMaintenanceDTO dto) {

        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(dto.registrationNumber()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found with registration: " + dto.registrationNumber()));

        MaintenanceTemplate template = templateRepository.findById(dto.templateId())
                .orElseThrow(() -> new IllegalArgumentException("Maintenance template not found with ID: " + dto.templateId()));

        MaintenanceRecord record = new MaintenanceRecord(
                aircraft,
                template,
                dto.description(),
                dto.expectedDuration(),
                dto.componentCategory()
        );
        return recordRepository.save(record);
    }

    // US116 - View Maintenance History for a Specific Aircraft
    public List<MaintenanceRecord> getAircraftHistory(String registrationNumber) {
        return recordRepository.findByAircraft_RegistrationNumber(new RegistrationNumber(registrationNumber));
    }

    // US117 - Calculate Fleet Total Maintenance Hours
    public Integer getTotalMaintenanceHours() {
        return recordRepository.findAll().stream()
                .mapToInt(MaintenanceRecord::getExpectedDuration)
                .sum();
    }

    // US119 - Conclude Maintenance Activity
    public MaintenanceRecord completeMaintenance(Long recordId, CompleteMaintenanceDTO dto) {
        MaintenanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found with ID: " + recordId));

        record.complete(dto.completionNotes(), dto.cost());
        return recordRepository.save(record);
    }
}