package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftModelDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAircraftStatusDTO;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;

import java.time.LocalDate;
import java.util.List;

@Order(2)
@Component
public class AircraftBootstrapper implements CommandLineRunner {

    private final AircraftService aircraftService;
    private final AircraftRepository aircraftRepository;

    public AircraftBootstrapper(AircraftService aircraftService, AircraftRepository aircraftRepository) {
        this.aircraftService = aircraftService;
        this.aircraftRepository = aircraftRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (aircraftRepository.count() > 0) {
            System.out.println("BOOTSTRAP: Data already exists. Skipping aircraft generation.");
            return;
        }

        System.out.println("BOOTSTRAP: Generating realistic fleet data...");

        // Adicionados os campos obrigatórios: fuelConsumption, seatingConfiguration, operatingHoursRange
        aircraftService.registerModel(new RegisterAircraftModelDTO("A320neo", "AIRBUS", 180, 26730.0, 6300.0, 833.0, 2500.0, null, "3-3", "24/7", "Turbofan"));
        aircraftService.registerModel(new RegisterAircraftModelDTO("B737 MAX", "BOEING", 210, 25800.0, 6570.0, 839.0, 2600.0, null, "3-3", "24/7", "Turbofan"));
        aircraftService.registerModel(new RegisterAircraftModelDTO("A350-900", "AIRBUS", 350, 141000.0, 15000.0, 903.0, 5000.0, null, "3-3-3", "24/7", "Turbofan"));
        aircraftService.registerModel(new RegisterAircraftModelDTO("B777-300ER", "BOEING", 396, 181280.0, 13650.0, 892.0, 5500.0, null, "3-4-3", "24/7", "Turbofan"));
        aircraftService.registerModel(new RegisterAircraftModelDTO("E190-E2", "EMBRAER", 114, 13690.0, 5278.0, 870.0, 1800.0, null, "2-2", "24/7", "Geared Turbofan"));
        aircraftService.registerModel(new RegisterAircraftModelDTO("ATR 72-600", "ATR", 78, 5000.0, 1528.0, 510.0, 800.0, null, "2-2", "24/7", "Turboprop"));

        aircraftService.registerAircraft(new RegisterAircraftDTO("CS-TPA", "A320neo", LocalDate.of(2019, 5, 10), 180, "WiFi"));
        aircraftService.registerAircraft(new RegisterAircraftDTO("CS-TVB", "A320neo", LocalDate.of(2020, 8, 22), 180, "None"));
        aircraftService.registerAircraft(new RegisterAircraftDTO("CS-BOA", "B737 MAX", LocalDate.of(2022, 3, 5), 200, "Power Outlets"));
        aircraftService.registerAircraft(new RegisterAircraftDTO("CS-XPA", "A350-900", LocalDate.of(2018, 11, 2), 350, "WiFi"));
        aircraftService.registerAircraft(new RegisterAircraftDTO("CS-EMA", "E190-E2", LocalDate.of(2021, 10, 20), 114, "None"));
        aircraftService.registerAircraft(new RegisterAircraftDTO("CS-ATA", "ATR 72-600", LocalDate.of(2017, 6, 8), 78, "None"));

        List<Aircraft> fleet = aircraftRepository.findAll();
        for (Aircraft a : fleet) {
            a.addFlightHours(2000.0);
            aircraftRepository.save(a);
        }

        System.out.println("BOOTSTRAP: Fleet data loaded successfully!");
    }
}