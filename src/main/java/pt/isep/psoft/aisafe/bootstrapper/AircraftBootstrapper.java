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
import pt.isep.psoft.aisafe.domain.RegistrationNumber;
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
        try {

            if (aircraftRepository.count() > 0) {
                System.out.println("BOOTSTRAP: Data already exists. Skipping aircraft generation.");
                return;
            }

            System.out.println("BOOTSTRAP: Generating realistic fleet data...");

            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "A320neo", "AIRBUS", 180, 26730.0, 6300.0, 833.0,
                    "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=800&q=80"));

            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "B737 MAX", "BOEING", 210, 25800.0, 6570.0, 839.0,
                    "https://images.unsplash.com/photo-1542296332-2e4473faf563?auto=format&fit=crop&w=800&q=80"));

            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "A350-900", "AIRBUS", 350, 141000.0, 15000.0, 903.0,
                    "https://images.unsplash.com/photo-1556388158-158ea5cc21f8?auto=format&fit=crop&w=800&q=80"));

            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "B777-300ER", "BOEING", 396, 181280.0, 13650.0, 892.0,
                    "https://images.unsplash.com/photo-1569154941061-e231b4732ef1?auto=format&fit=crop&w=800&q=80"));

            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "E190-E2", "EMBRAER", 114, 13690.0, 5278.0, 870.0,
                    "https://images.unsplash.com/photo-1587528434857-e17911b3b190?auto=format&fit=crop&w=800&q=80"));

            aircraftService.registerModel(new RegisterAircraftModelDTO(
                    "ATR 72-600", "ATR", 78, 5000.0, 1528.0, 510.0,
                    "https://images.unsplash.com/photo-1512353087810-2580f4659729?auto=format&fit=crop&w=800&q=80"));


            // Airbus A320neo Fleet
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-TPA", "A320neo", LocalDate.of(2019, 5, 10), 180));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-TVB", "A320neo", LocalDate.of(2020, 8, 22), 180));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-TVC", "A320neo", LocalDate.of(2021, 1, 15), 180));

            // Boeing 737 MAX Fleet
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-BOA", "B737 MAX", LocalDate.of(2022, 3, 5), 200));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-BOB", "B737 MAX", LocalDate.of(2023, 7, 11), 200));

            // Long Haul Fleet (A350 & B777)
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-XPA", "A350-900", LocalDate.of(2018, 11, 2), 350));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-XPB", "A350-900", LocalDate.of(2019, 4, 18), 350));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-TTA", "B777-300ER", LocalDate.of(2015, 9, 30), 396));

            // Regional Fleet (Embraer & ATR)
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-EMA", "E190-E2", LocalDate.of(2021, 10, 20), 114));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-EMB", "E190-E2", LocalDate.of(2022, 2, 14), 114));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-ATA", "ATR 72-600", LocalDate.of(2017, 6, 8), 78));
            aircraftService.registerAircraft(new RegisterAircraftDTO("CS-ATB", "ATR 72-600", LocalDate.of(2017, 12, 1), 78));


            List<Aircraft> fleet = aircraftRepository.findAll();
            for (Aircraft a : fleet) {
                int age = LocalDate.now().getYear() - a.getManufacturingDate().getYear();
                double simulatedHours = age * 2500.5 + 500.0;

                a.addFlightHours(simulatedHours);
                aircraftRepository.save(a);
            }

            aircraftService.updateAircraftStatus("CS-TVB", new UpdateAircraftStatusDTO("UNDER_MAINTENANCE"));
            aircraftService.updateAircraftStatus("CS-ATB", new UpdateAircraftStatusDTO("INACTIVE"));
            aircraftService.updateAircraftStatus("CS-TTA", new UpdateAircraftStatusDTO("UNDER_MAINTENANCE"));

            System.out.println("BOOTSTRAP: WP#0B - 6 Models and 12 Aircraft loaded successfully with operational hours!");

        } catch (Exception e) {
            System.out.println("An error occurred in the aircraft's Bootstrap: " + e.getMessage());
        }
    }
}