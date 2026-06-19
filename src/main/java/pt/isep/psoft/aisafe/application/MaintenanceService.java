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

        java.time.LocalDateTime startDate = java.time.LocalDateTime.parse(dto.startDate());

        MaintenanceRecord record = new MaintenanceRecord(
                aircraft,
                template,
                dto.description(),
                dto.expectedDuration(),
                dto.componentCategory(),
                startDate,
                dto.cost()
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
                .filter(record -> record.getStatus() == MaintenanceRecordStatus.COMPLETED)
                .mapToInt(MaintenanceRecord::getExpectedDuration)
                .sum();
    }

    // US119 - Conclude Maintenance Activity
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

    // US219 - View ongoing maintenance activities
    public List<MaintenanceRecord> getOngoingMaintenances() {
        return recordRepository.findByStatus(MaintenanceRecordStatus.SCHEDULED);
    }

    // US220: Generate maintenance costs report per aircraft
    public List<MaintenanceCostDTO> getMaintenanceCostsPerAircraft() {
        List<Object[]> results = recordRepository.getMaintenanceCostsByAircraft();

        return results.stream()
                .map(row -> new MaintenanceCostDTO(
                        (String) row[0],
                        (Double) row[1]
                ))
                .collect(Collectors.toList());
    }

    // US221: Average turnaround time per aircraft model
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

    // --- US222: Generate Maintenance Alerts ---
    public List<MaintenanceAlertDTO> generateMaintenanceAlerts() {
        List<MaintenanceAlertDTO> alerts = new java.util.ArrayList<>();
        List<Aircraft> allAircraft = aircraftRepository.findAll();

        for (Aircraft aircraft : allAircraft) {
            if (aircraft.getStatus() != AircraftStatus.ACTIVE) continue;

            List<MaintenanceTemplate> templates = templateRepository.findByAppliesToContaining(aircraft.getAircraftModel());
            for (MaintenanceTemplate template : templates) {

                // VERIFICATION A: Flight Hours Limit check
                if (template.getFlightHoursInterval() != null) {
                    if (aircraft.getTotalFlightHours() >= template.getFlightHoursInterval()) {
                        alerts.add(new MaintenanceAlertDTO(
                                aircraft.getRegistrationNumber().toString(),
                                "FLIGHT_HOURS",
                                "Aircraft reached " + aircraft.getTotalFlightHours() + " hours. Template limit: " + template.getFlightHoursInterval()
                        ));
                    }
                }

                // VERIFICATION B: Calendar Days Interval check with dynamic safety margin
                if (template.getCalendarDaysInterval() != null) {
                    java.util.Optional<MaintenanceRecord> lastRecord = recordRepository.findTopByAircraftAndStatusOrderByCompletionDateDesc(
                            aircraft, MaintenanceRecordStatus.COMPLETED
                    );

                    java.time.LocalDate referenceDate;
                    if (lastRecord.isPresent() && lastRecord.get().getCompletionDate() != null) {
                        referenceDate = lastRecord.get().getCompletionDate().toLocalDate();
                    } else {
                        referenceDate = aircraft.getManufacturingDate();
                    }

                    long daysPassed = java.time.temporal.ChronoUnit.DAYS.between(referenceDate, java.time.LocalDate.now());

                    // Safety Buffer Configuration (15 days ahead alert window)
                    int safetyMargin = 15;

                    if (daysPassed >= (template.getCalendarDaysInterval() - safetyMargin)) {
                        long daysLeft = template.getCalendarDaysInterval() - daysPassed;
                        String alertMessage;

                        if (daysLeft < 0) {
                            alertMessage = "CRITICAL OVERDUE: Aircraft is " + Math.abs(daysLeft) + " days past the safety threshold (" + template.getCalendarDaysInterval() + " days limit).";
                        } else {
                            alertMessage = "PREVENTIVE WARNING: Upcoming maintenance must be performed within " + daysLeft + " days. (Limit: " + template.getCalendarDaysInterval() + " days).";
                        }

                        alerts.add(new MaintenanceAlertDTO(
                                aircraft.getRegistrationNumber().toString(),
                                "CALENDAR_DAYS",
                                alertMessage
                        ));
                    }
                }
            }
        }
        return alerts;
    }
}