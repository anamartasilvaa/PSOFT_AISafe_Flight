package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.AirportService;
import pt.isep.psoft.aisafe.application.DTO.AddCertificationDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAirportDTO;
import pt.isep.psoft.aisafe.application.DTO.RunwayDTO;

import java.time.LocalDate;
import java.util.List;

@Component
@Order(3)
public class AirportBootstrapper implements CommandLineRunner {

    private final AirportService airportService;

    public AirportBootstrapper(AirportService airportService) {
        this.airportService = airportService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // --- 1. CRIAR OS AEROPORTOS E PISTAS ---


            airportService.registerAirport(new RegisterAirportDTO(
                    "OPO", "Aeroporto Francisco Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon",
                    "INTERNATIONAL", 41.2356, -8.6781,
                    List.of(new RunwayDTO("17/35", 3480.0, "North-South")),
                    List.of(), // Facilities vazias no arranque
                    null       // Imagem nula no arranque
            ));


            airportService.registerAirport(new RegisterAirportDTO(
                    "LIS", "Aeroporto Humberto Delgado", "Lisbon", "Portugal", "Europe/Lisbon",
                    "INTERNATIONAL", 38.7742, -9.1342,
                    List.of(new RunwayDTO("03/21", 3805.0, "North-South")),
                    List.of(), // Facilities vazias no arranque
                    null       // Imagem nula no arranque
            ));

            airportService.registerAirport(new RegisterAirportDTO(
                    "JFK", "John F. Kennedy International Airport", "New York", "USA", "America/New_York",
                    "INTERNATIONAL", 40.6413, -73.7781,
                    List.of(new RunwayDTO("04L/22R", 3460.0, "Northeast-Southwest"),
                            new RunwayDTO("13R/31L", 4442.0, "Northwest-Southeast")), // JFK tem várias pistas!
                    List.of(), // Facilities vazias no arranque
                    null       // Imagem nula no arranque
            ));

            // --- 2. ADICIONAR CERTIFICAÇÕES (US106a) ---


            LocalDate issueDate = LocalDate.now();
            LocalDate expiryDate = LocalDate.now().plusYears(5);


            airportService.addCertification("OPO", new AddCertificationDTO(
                    "CERT-OPO-001", "A320neo", issueDate, expiryDate
            ));


            airportService.addCertification("LIS", new AddCertificationDTO(
                    "CERT-LIS-001", "B737 MAX", issueDate, expiryDate
            ));


            airportService.addCertification("JFK", new AddCertificationDTO(
                    "CERT-JFK-001", "A320neo", issueDate, expiryDate
            ));
            airportService.addCertification("JFK", new AddCertificationDTO(
                    "CERT-JFK-002", "B737 MAX", issueDate, expiryDate
            ));

            System.out.println("BOOTSTRAP: 3 Airports (OPO, LIS, JFK) with runways and certifications successfully added!");

        } catch (Exception e) {
            System.out.println("Note: Airports/Certifications might already exist or: " + e.getMessage());
        }
    }
}