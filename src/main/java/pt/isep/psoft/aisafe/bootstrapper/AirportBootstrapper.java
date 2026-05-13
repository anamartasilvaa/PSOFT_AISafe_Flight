package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.AirportService;
import pt.isep.psoft.aisafe.application.DTO.RegisterAirportDTO;
import pt.isep.psoft.aisafe.application.DTO.RunwayDTO;
import java.util.List;

@Component
@Order(2) // Executa depois do AircraftBootstrapper se necessário
public class AirportBootstrapper implements CommandLineRunner {

    private final AirportService airportService;

    public AirportBootstrapper(AirportService airportService) {
        this.airportService = airportService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Inserir o Porto (OPO) para os teus testes de GET serem automáticos
            airportService.registerAirport(new RegisterAirportDTO(
                    "OPO",
                    "Aeroporto Francisco Sá Carneiro",
                    "Porto",
                    "Portugal",
                    "Europe/Lisbon",
                    "INTERNATIONAL",
                    41.2356,
                    -8.6781,
                    List.of(new RunwayDTO("17/35", 3480.0, "North-South"))
            ));

            System.out.println("BOOTSTRAP: Initial airport (OPO) successfully added!");

        } catch (Exception e) {
            System.out.println("Note: Airport OPO might already exist or: " + e.getMessage());
        }
    }
}