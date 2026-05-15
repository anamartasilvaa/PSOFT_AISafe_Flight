package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftModelDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAircraftStatusDTO;

import java.time.LocalDate;

@Component
public class AircraftBootstrapper implements CommandLineRunner {

    private final AircraftService aircraftService;

    public AircraftBootstrapper(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
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