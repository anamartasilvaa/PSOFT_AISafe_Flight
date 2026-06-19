package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.ComponentCategory;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.MaintenanceRecordRepository;
import pt.isep.psoft.aisafe.repositories.MaintenanceTemplateRepository;

import java.time.LocalDateTime;
import java.util.List;

@Order(4)
@Component
public class MaintenanceRecordBootstrapper implements CommandLineRunner {

    private final MaintenanceRecordRepository recordRepository;
    private final AircraftRepository aircraftRepository;
    private final MaintenanceTemplateRepository templateRepository;

    public MaintenanceRecordBootstrapper(MaintenanceRecordRepository recordRepository,
                                         AircraftRepository aircraftRepository,
                                         MaintenanceTemplateRepository templateRepository) {
        this.recordRepository = recordRepository;
        this.aircraftRepository = aircraftRepository;
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            if (recordRepository.count() > 0) {
                System.out.println("BOOTSTRAP: Maintenance Records already exist. Skipping generation.");
                return;
            }

            System.out.println("BOOTSTRAP: Generating precise maintenance records for the fleet...");

            List<MaintenanceTemplate> templates = (List<MaintenanceTemplate>) templateRepository.findAll();
            if (templates.isEmpty()) {
                System.out.println("BOOTSTRAP ERROR: No templates found.");
                return;
            }

            MaintenanceTemplate defaultTemplate = templates.get(0);
            int limit = defaultTemplate.getCalendarDaysInterval() != null ? defaultTemplate.getCalendarDaysInterval() : 120;

            List<Aircraft> allAircraft = aircraftRepository.findAll();

            for (Aircraft aircraft : allAircraft) {
                String regNum = aircraft.getRegistrationNumber().toString();
                LocalDateTime historicalDate;
                String description;

                // 1. SCENARIO: PREVENTIVE WARNING (Leaves exactly 10 days before reaching the limit)
                if (regNum.equals("CS-TPA")) {
                    historicalDate = LocalDateTime.now().minusDays(limit - 10);
                    description = "Engine inspection (Preventive Scenario)";
                }
                // 2. SCENARIO: CRITICAL OVERDUE (5 days past the limit)
                else if (regNum.equals("CS-BOA")) {
                    historicalDate = LocalDateTime.now().minusDays(limit + 5);
                    description = "Navigation update (Overdue Scenario)";
                }
                // 3. SCENARIO: HEALTHY FLEET (Recent maintenance, 30 days ago)
                else {
                    historicalDate = LocalDateTime.now().minusDays(30);
                    description = "Standard Transit Check";
                }

                // Create and complete the record
                MaintenanceRecord record = new MaintenanceRecord(
                        aircraft, defaultTemplate, description,
                        120, ComponentCategory.AIRFRAME, historicalDate.minusDays(1), 500.0
                );

                // This method internally sets the completion date to TODAY (LocalDateTime.now())
                record.complete("Completed standard procedures.");

                // --- ENGINEER'S TRICK: JAVA REFLECTION ---
                // We bypass the domain encapsulation just for this bootstrapper to force the historical date.
                try {
                    java.lang.reflect.Field dateField = null;
                    Class<?> clazz = record.getClass();

                    // Search for the 'completionDate' field in the class (or superclasses)
                    while (clazz != null && dateField == null) {
                        try {
                            dateField = clazz.getDeclaredField("completionDate");
                        } catch (NoSuchFieldException e) {
                            clazz = clazz.getSuperclass();
                        }
                    }

                    if (dateField != null) {
                        dateField.setAccessible(true);
                        dateField.set(record, historicalDate); // Overwrite TODAY with our historical date
                    }
                } catch (Exception e) {
                    System.out.println("Reflection failed: Could not forcefully set completion date: " + e.getMessage());
                }

                recordRepository.save(record);
            }

            System.out.println("BOOTSTRAP: Precise Maintenance Records loaded successfully!");

        } catch (Exception e) {
            System.out.println("An error occurred in Maintenance Record Bootstrap: " + e.getMessage());
        }
    }
}