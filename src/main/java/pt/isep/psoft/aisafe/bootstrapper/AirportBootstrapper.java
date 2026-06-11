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

            // 3. WP2B
            System.out.println("BOOTSTRAP WP2B: Adding new airports with facilities and updating existing ones...");


            airportService.updateAirportDetails("OPO", new pt.isep.psoft.aisafe.application.DTO.UpdateAirportDetailsDTO(
                    "24/7 Operations",
                    "Phone: +351 229 432 400 | Email: porto@ana.pt"
            ));

            airportService.registerAirport(new RegisterAirportDTO(
                    "CDG", "Aéroport de Paris-Charles de Gaulle", "Paris", "France", "Europe/Paris",
                    "INTERNATIONAL", 49.0097, 2.5479,
                    List.of(new RunwayDTO("09L/27R", 4200.0, "East-West")),
                    List.of(
                            new pt.isep.psoft.aisafe.application.DTO.FacilityDTO("Terminal", "Terminal 1 - Star Alliance"),
                            new pt.isep.psoft.aisafe.application.DTO.FacilityDTO("Lounge", "Air France VIP Lounge")
                    ),
                    "https://images.unsplash.com/photo-1542273917-6d6014e08c84"
            ));

            airportService.registerAirport(new RegisterAirportDTO(
                    "LHR", "Heathrow Airport", "London", "UK", "Europe/London",
                    "INTERNATIONAL", 51.4700, -0.4543,
                    List.of(new RunwayDTO("09L/27R", 3902.0, "East-West")),
                    List.of(new pt.isep.psoft.aisafe.application.DTO.FacilityDTO("Terminal", "Terminal 5")),
                    "https://images.unsplash.com/photo-1576883600124-64c5aa295b28"
            ));

        } catch (Exception e) {
            System.out.println("Note: Airports/Certifications might already exist or: " + e.getMessage());
        }
    }
}