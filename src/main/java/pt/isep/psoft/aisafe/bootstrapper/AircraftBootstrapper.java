package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftModelDTO;

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
            // 1. Criar o "Molde" A320neo automaticamente
            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "A320neo",
                    "AIRBUS",
                    180,
                    26730.0,
                    6300.0,
                    833.0,
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Airbus_A320neo_House_Colors.jpg/800px-Airbus_A320neo_House_Colors.jpg"
            ));

            // 2. Criar o avião "CS-TPA" associado a esse molde
            aircraftService.registerAircraft(new RegisterAircraftDTO(
                    "CS-TPA",
                    "A320neo",
                    LocalDate.of(2024, 5, 10),
                    180
            ));

            System.out.println("BOOTSTRAP: Initial aircraft successfully added to the database!");

        } catch (Exception e) {
            System.out.println("An error occurred in the aircraft's Bootstrap: " + e.getMessage());
        }
    }
}