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
        System.out.println("BOOTSTRAP WP3: Setting up US216 Test with EXISTING airports...");

        // --- A SOLUÇÃO PLUG & PLAY PARA A US216 ---

        // Alternativa 1: Voo Direto (Mais rápido: 140 min)
        saveRouteSafe("RT-LISLHR", "LIS", "LHR", 140, 1500.0, 150);

        // Alternativa 2: Voo com Escala no Porto (45m + 60m MCT + 130m = 235 min)
        saveRouteSafe("RT-LISOPO", "LIS", "OPO", 45, 250.0, 70);
        saveRouteSafe("RT-OPOLHR", "OPO", "LHR", 130, 1330.0, 120);

        // --- Restantes rotas originais da tua Base de Dados ---
        saveRouteSafe("RT-OPOLIS", "OPO", "LIS", 45, 250.0, 70);
        saveRouteSafe("RT-LISJFK", "LIS", "JFK", 480, 5000.0, 150);
        saveRouteSafe("RT-JFKLIS", "JFK", "LIS", 480, 5000.0, 150);
        saveRouteSafe("RT-LISCDG", "LIS", "CDG", 150, 1530.0, 150);
        saveRouteSafe("RT-CDGLIS", "CDG", "LIS", 155, 1530.0, 150);
        saveRouteSafe("RT-LHROPO", "LHR", "OPO", 135, 1330.0, 120);
        saveRouteSafe("RT-LHRJFK", "LHR", "JFK", 460, 5540.0, 250);
        saveRouteSafe("RT-CDGJFK", "CDG", "JFK", 490, 5830.0, 250);

        System.out.println("BOOTSTRAP WP3: Route verification complete.");
    }

    private void saveRouteSafe(String routeId, String origin, String destination, int time, double range, int capacity) {
        try {
            routeService.createRoute(new CreateRouteDTO(routeId, origin, destination, time, range, capacity));
        } catch (Exception e) {
        }
    }
}