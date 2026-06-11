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
            )
            );

            System.out.println("BOOTSTRAP: 2 Routes (OPO-LIS, LIS-JFK) successfully added!");

            // --- WP2B:
            System.out.println("BOOTSTRAP WP2B: Expanding route network for statistics...");

            routeService.createRoute(new CreateRouteDTO("RT-LISOPO", "LIS", "OPO", 45, 250.0, 100));
            routeService.createRoute(new CreateRouteDTO("RT-JFKLIS", "JFK", "LIS", 480, 5000.0, 150));

            routeService.createRoute(new CreateRouteDTO("RT-LISCDG", "LIS", "CDG", 150, 1530.0, 150));
            routeService.createRoute(new CreateRouteDTO("RT-CDGLIS", "CDG", "LIS", 155, 1530.0, 150));

            routeService.createRoute(new CreateRouteDTO("RT-OPOLHR", "OPO", "LHR", 130, 1330.0, 120));
            routeService.createRoute(new CreateRouteDTO("RT-LHROPO", "LHR", "OPO", 135, 1330.0, 120));

            routeService.createRoute(new CreateRouteDTO("RT-LHRJFK", "LHR", "JFK", 460, 5540.0, 250));
            routeService.createRoute(new CreateRouteDTO("RT-CDGJFK", "CDG", "JFK", 490, 5830.0, 250));

        } catch (Exception e) {
            System.out.println("Note: Routes might already exist or: " + e.getMessage());
        }
    }
}