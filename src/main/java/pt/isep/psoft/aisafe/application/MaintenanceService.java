package pt.isep.psoft.aisafe.application;

package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceService {

    private final MaintenanceTemplateRepository templateRepository;
    private final MaintenanceRecordRepository recordRepository;
    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final MaintenancePartRepository partRepository;

    public MaintenanceService(MaintenanceTemplateRepository templateRepository,
                              MaintenanceRecordRepository recordRepository,
                              AircraftRepository aircraftRepository,
                              AircraftModelRepository aircraftModelRepository,
                              MaintenancePartRepository partRepository) {
        this.templateRepository = templateRepository;
        this.recordRepository = recordRepository;
        this.aircraftRepository = aircraftRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.partRepository = partRepository;
    }

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
                dto.templateName(), dto.templateType(), dto.flightHoursInterval(), dto.calendarDaysInterval(), checklist, models
        );
        return templateRepository.save(template);
    }

    public MaintenanceRecord registerMaintenance(RegisterMaintenanceDTO dto) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(dto.registrationNumber()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found with registration: " + dto.registrationNumber()));
        MaintenanceTemplate template = templateRepository.findById(dto.templateId())
                .orElseThrow(() -> new IllegalArgumentException("Maintenance template not found with ID: " + dto.templateId()));
        java.time.LocalDateTime startDate = java.time.LocalDateTime.parse(dto.startDate());
        MaintenanceRecord record = new MaintenanceRecord(
                aircraft, template, dto.description(), dto.expectedDuration(), dto.componentCategory(), startDate, dto.cost()
        );
        return recordRepository.save(record);
    }

    public List<MaintenanceRecord> getAircraftHistory(String registrationNumber) {
        return recordRepository.findByAircraft_RegistrationNumber(new RegistrationNumber(registrationNumber));
    }

    public Integer getTotalMaintenanceHours() {
        return recordRepository.findAll().stream()
                .filter(record -> record.getStatus() == MaintenanceRecordStatus.COMPLETED)
                .mapToInt(MaintenanceRecord::getExpectedDuration)
                .sum();
    }

    public Integer getTotalMaintenanceHoursByAircraft(String registrationNumber) {
        return recordRepository.findByAircraft_RegistrationNumber(new RegistrationNumber(registrationNumber))
                .stream()
                .filter(record -> record.getStatus() == MaintenanceRecordStatus.COMPLETED)
                .mapToInt(MaintenanceRecord::getExpectedDuration)
                .sum();
    }

    public MaintenanceRecord completeMaintenance(Long recordId, CompleteMaintenanceDTO dto) {
        MaintenanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found with ID: " + recordId));
        record.complete(dto.completionNotes());
        return recordRepository.save(record);
    }

    public org.springframework.data.domain.Page<MaintenanceRecord> searchMaintenanceRecords(
            String registrationNumber, String fromDate, String toDate, String category, org.springframework.data.domain.Pageable pageable) {
        java.time.LocalDateTime start = (fromDate != null && !fromDate.isBlank()) ? java.time.LocalDate.parse(fromDate).atStartOfDay() : null;
        java.time.LocalDateTime end = (toDate != null && !toDate.isBlank()) ? java.time.LocalDate.parse(toDate).atTime(23, 59, 59) : null;
        ComponentCategory cat = null;
        if (category != null && !category.isBlank()) {
            cat = ComponentCategory.valueOf(category.toUpperCase());
        }
        return recordRepository.searchRecords(registrationNumber, start, end, cat, pageable);
    }

    // US219 - Corrigido para IN_PROGRESS
    public List<MaintenanceRecord> getOngoingMaintenances() {
        return recordRepository.findByStatus(MaintenanceRecordStatus.IN_PROGRESS);
    }

    // US220: Generate maintenance costs report (by aircraft or model)
    public List<MaintenanceCostDTO> getMaintenanceCosts(String groupBy) {
        List<Object[]> results;
        if ("model".equalsIgnoreCase(groupBy)) {
            results = recordRepository.getMaintenanceCostsByAircraftModel();
        } else {
            results = recordRepository.getMaintenanceCostsByAircraft();
        }
        return results.stream()
                .map(row -> new MaintenanceCostDTO(row[0].toString(), (Double) row[1]))
                .collect(Collectors.toList());
    }

    public List<TurnaroundTimeDTO> getTurnaroundTimePerAircraftModel() {
        List<MaintenanceRecord> completedRecords = recordRepository.findByStatus(MaintenanceRecordStatus.COMPLETED);
        java.util.Map<String, List<MaintenanceRecord>> recordsByModel = completedRecords.stream()
                .filter(r -> r.getCompletionDate() != null)
                .collect(Collectors.groupingBy(r -> r.getAircraft().getAircraftModel().getModelName().toString()));
        return recordsByModel.entrySet().stream()
                .map(entry -> {
                    String modelName = entry.getKey();
                    List<MaintenanceRecord> records = entry.getValue();
                    double averageHours = records.stream()
                            .mapToLong(r -> java.time.temporal.ChronoUnit.HOURS.between(r.getStartDate(), r.getCompletionDate()))
                            .average()
                            .orElse(0.0);
                    return new TurnaroundTimeDTO(modelName, averageHours);
                })
                .collect(Collectors.toList());
    }

    public List<MaintenanceAlertDTO> generateMaintenanceAlerts() {
        List<MaintenanceAlertDTO> alerts = new java.util.ArrayList<>();
        List<Aircraft> allAircraft = aircraftRepository.findAll();
        for (Aircraft aircraft : allAircraft) {
            if (aircraft.getStatus() != AircraftStatus.ACTIVE) continue;
            List<MaintenanceTemplate> templates = templateRepository.findByAppliesToContaining(aircraft.getAircraftModel());
            for (MaintenanceTemplate template : templates) {
                if (template.getFlightHoursInterval() != null) {
                    if (aircraft.getTotalFlightHours() >= template.getFlightHoursInterval()) {
                        alerts.add(new MaintenanceAlertDTO(aircraft.getRegistrationNumber().toString(), "FLIGHT_HOURS", "Limit reached."));
                    }
                }
                if (template.getCalendarDaysInterval() != null) {
                    java.util.Optional<MaintenanceRecord> lastRecord = recordRepository.findTopByAircraftAndStatusOrderByCompletionDateDesc(aircraft, MaintenanceRecordStatus.COMPLETED);
                    java.time.LocalDate referenceDate = lastRecord.isPresent() && lastRecord.get().getCompletionDate() != null ? lastRecord.get().getCompletionDate().toLocalDate() : aircraft.getManufacturingDate();
                    long daysPassed = java.time.temporal.ChronoUnit.DAYS.between(referenceDate, java.time.LocalDate.now());
                    if (daysPassed >= (template.getCalendarDaysInterval() - 15)) {
                        alerts.add(new MaintenanceAlertDTO(aircraft.getRegistrationNumber().toString(), "CALENDAR_DAYS", "Upcoming maintenance warning."));
                    }
                }
            }
        }
        return alerts;
    }

    public List<LowStockAlertDTO> generateLowStockAlerts() {
        return partRepository.findPartsWithLowStock().stream()
                .map(part -> new LowStockAlertDTO(part.getPartNumber().getNumber(), part.getName(), part.getStockQuantity(), part.getMinimumThreshold(), "CRITICAL"))
                .collect(Collectors.toList());
    }
}