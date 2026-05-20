package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftModelDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAircraftStatusDTO;
import pt.isep.psoft.aisafe.domain.Checklist;
import pt.isep.psoft.aisafe.domain.ChecklistItem;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;
import pt.isep.psoft.aisafe.repositories.MaintenanceTemplateRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class AircraftBootstrapper implements CommandLineRunner {

    private final AircraftService aircraftService;
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;

    public AircraftBootstrapper(AircraftService aircraftService,
                                MaintenanceTemplateRepository maintenanceTemplateRepository) {
        this.aircraftService = aircraftService;
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {

            // 1. BOOTSTRAP: MAINTENANCE TEMPLATE AGGREGATE

            if (maintenanceTemplateRepository.count() == 0) {

                // --- BUILD CHECK A ---
                //Create Value Objects (ChecklistItem)
                List<ChecklistItem> itemsA = new ArrayList<>();
                itemsA.add(new ChecklistItem("Check tire pressure", true));
                itemsA.add(new ChecklistItem("Inspect fluid levels", true));

                //Create Entity (Checklist) and associate items
                Checklist checklistA = new Checklist("Daily Checklist - Check A", "v1.0", itemsA);

                // Create Aggregate Root (MaintenanceTemplate) and associate Checklist
                MaintenanceTemplate templateA = new MaintenanceTemplate(
                        "Check A",
                        "PREVENTIVE",
                        125.0,
                        90,
                        checklistA,
                        new ArrayList<>() // appliesTo (empty for now)
                );


                maintenanceTemplateRepository.save(templateA);


                // --- BUILD CHECK B ---
                List<ChecklistItem> itemsB = new ArrayList<>();
                itemsB.add(new ChecklistItem("Deep structural inspection", true));
                itemsB.add(new ChecklistItem("Avionics calibration", true));
                itemsB.add(new ChecklistItem("Replace aesthetic elements", false));

                Checklist checklistB = new Checklist("Annual Checklist - Check B", "v2.0", itemsB);

                MaintenanceTemplate templateB = new MaintenanceTemplate(
                        "Check B",
                        "DEEP_INSPECTION",
                        750.0,
                        365,
                        checklistB,
                        new ArrayList<>()
                );

                maintenanceTemplateRepository.save(templateB);

                System.out.println("BOOTSTRAP: Maintenance Template Aggregates successfully created!");
            }

            // 2. CRIAR OS MODELOS E AS AERONAVES


            // --- 1. CRIAR OS MODELOS ---
            // Modelo 1: A320neo
            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "A320neo", "AIRBUS", 180, 26730.0, 6300.0, 833.0,
                    "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=800&q=80"));

            // Modelo 2: B737 MAX (Para teres dois fabricantes e modelos diferentes na pesquisa)
            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "B737 MAX", "BOEING", 210, 25800.0, 6570.0, 839.0,
                    "https://images.unsplash.com/photo-1542296332-2e4473faf563?auto=format&fit=crop&w=800&q=80"));

            // --- 2. CRIAR AS AERONAVES ---
            // Avião 1: CS-TPA (Fica 'ACTIVE' por defeito, ano 2024)
            aircraftService.registerAircraft(new RegisterAircraftDTO(
                    "CS-TPA", "A320neo", LocalDate.of(2024, 5, 10), 180));

            // Avião 2: CS-TPB (Fica 'INACTIVE', ano 2020)
            aircraftService.registerAircraft(new RegisterAircraftDTO(
                    "CS-TPB", "A320neo", LocalDate.of(2020, 3, 15), 180));
            // Forçar a mudança de status usando o teu método (US105)
            aircraftService.updateAircraftStatus("CS-TPB", new UpdateAircraftStatusDTO("INACTIVE"));

            // Avião 3: CS-BOE (Fica 'UNDER_MAINTENANCE', ano 2023)
            aircraftService.registerAircraft(new RegisterAircraftDTO(
                    "CS-BOE", "B737 MAX", LocalDate.of(2023, 8, 20), 200));
            // Forçar a mudança de status
            aircraftService.updateAircraftStatus("CS-BOE", new UpdateAircraftStatusDTO("UNDER_MAINTENANCE"));

            System.out.println("BOOTSTRAP: 2 Models and 3 Aircraft successfully added to the database!");

        } catch (Exception e) {
            System.out.println("An error occurred in the aircraft's Bootstrap: " + e.getMessage());
        }
    }
}