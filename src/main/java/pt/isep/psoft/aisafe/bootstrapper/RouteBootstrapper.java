package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.RouteService;

@Component
@Order(4)
public class RouteBootstrapper implements CommandLineRunner {

    private final RouteService routeService;

    public RouteBootstrapper(RouteService routeService) {
        this.routeService = routeService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // Rota 1: Porto -> Lisboa (Curta duração)
            routeService.createRoute(new CreateRouteDTO(
                    "RT-OPOLIS", "OPO", "LIS", 45, 250.0, 100
            ));

            // Rota 2: Lisboa -> Nova Iorque (Longa duração)
            routeService.createRoute(new CreateRouteDTO(
                    "RT-LISJFK", "LIS", "JFK", 480, 5000.0, 150
            ));

            System.out.println("BOOTSTRAP: 2 Routes (OPO-LIS, LIS-JFK) successfully added!");

        } catch (Exception e) {
            System.out.println("Note: Routes might already exist or: " + e.getMessage());
        }
    }
}