package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.ComponentCategory;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;
import pt.isep.psoft.aisafe.domain.RegistrationNumber;
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

            System.out.println("BOOTSTRAP: Generating default Maintenance Records...");

            List<MaintenanceTemplate> templates = (List<MaintenanceTemplate>) templateRepository.findAll();
            if (templates.isEmpty()) {
                System.out.println("BOOTSTRAP ERROR: No templates found.");
                return;
            }

            MaintenanceTemplate defaultTemplate = templates.get(0);
            int limit = defaultTemplate.getCalendarDaysInterval() != null ? defaultTemplate.getCalendarDaysInterval() : 120;

            Aircraft a320 = aircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-TPA")).orElse(null);
            Aircraft a320Manut = aircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-MANUT")).orElse(null);
            Aircraft b737 = aircraftRepository.findByRegistrationNumber(new RegistrationNumber("CS-BOA")).orElse(null);

            if (a320 == null || a320Manut == null || b737 == null) {
                System.out.println("BOOTSTRAP ERROR: Required aircraft not found.");
                return;
            }

            // Record 1: Historical maintenance for CS-TPA (superseded by Record 4)
            MaintenanceRecord record1 = new MaintenanceRecord(
                    a320, defaultTemplate, "Engine oil replacement",
                    120, ComponentCategory.ENGINE, LocalDateTime.now().minusDays(limit + 100), 450.0
            );
            record1.complete("Completed successfully.");
            recordRepository.save(record1);

            // Record 2: Scheduled future maintenance task for CS-MANUT
            MaintenanceRecord record2 = new MaintenanceRecord(
                    a320Manut, defaultTemplate, "Landing gear inspection",
                    300, ComponentCategory.AIRFRAME, LocalDateTime.now().plusDays(1), 1200.0
            );
            recordRepository.save(record2);

            // Record 3: TRIGGER FOR CRITICAL OVERDUE ALERT (CS-BOA)
            // The completion date is 40 days past the allowed template interval
            MaintenanceRecord record3 = new MaintenanceRecord(
                    b737, defaultTemplate, "Navigation software update",
                    60, ComponentCategory.AVIONICS, LocalDateTime.now().minusDays(limit + 40), 200.0
            );
            record3.complete("Software updated to version 4.2.");
            recordRepository.save(record3);

            // Record 4: TRIGGER FOR PREVENTIVE WARNING ALERT (CS-TPA)
            // The completion date leaves exactly 10 days before reaching expiration (triggers within the 15 days margin)
            MaintenanceRecord record4 = new MaintenanceRecord(
                    a320, defaultTemplate, "Passenger seat repairs",
                    90, ComponentCategory.INTERIOR, LocalDateTime.now().minusDays(limit - 10), 150.0
            );
            record4.complete("Fixed fabric.");
            recordRepository.save(record4);

            System.out.println("BOOTSTRAP: Maintenance Records loaded successfully!");

        } catch (Exception e) {
            System.out.println("An error occurred in Maintenance Record Bootstrap: " + e.getMessage());
        }
    }
}