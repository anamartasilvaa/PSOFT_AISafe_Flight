package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.domain.AircraftModel;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import pt.isep.psoft.aisafe.repositories.MaintenanceTemplateRepository;

import java.util.ArrayList;
import java.util.List;

@Order(3)
@Component
public class MaintenanceTemplateBootstrapper implements CommandLineRunner {

    private final MaintenanceTemplateRepository templateRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public MaintenanceTemplateBootstrapper(MaintenanceTemplateRepository templateRepository,
                                           AircraftModelRepository aircraftModelRepository) {
        this.templateRepository = templateRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            if (templateRepository.count() > 0) {
                System.out.println("BOOTSTRAP: Maintenance Templates already exist. Skipping generation.");
                return;
            }

            System.out.println("BOOTSTRAP: Generating default Maintenance Templates...");

            Iterable<AircraftModel> models = aircraftModelRepository.findAll();
            List<AircraftModel> allModels = new ArrayList<>();
            models.forEach(allModels::add);

            if (allModels.isEmpty()) {
                System.out.println("BOOTSTRAP ERROR: No aircraft models found to associate with the Template.");
                return;
            }

            // Assigning the template to ALL models in the database
            MaintenanceTemplate template1 = new MaintenanceTemplate(
                    "Routine General Inspection (A-Check)",
                    "ROUTINE",
                    500.0,
                    120, // 120 days limit
                    null,
                    allModels
            );

            templateRepository.save(template1);

            System.out.println("BOOTSTRAP: Maintenance Templates loaded successfully!");

        } catch (Exception e) {
            System.out.println("An error occurred in the Maintenance Template Bootstrap: " + e.getMessage());
        }
    }
}