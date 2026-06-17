package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.FlightService;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.domain.FlightStatus;
import pt.isep.psoft.aisafe.domain.ScheduledFlight;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Order(5)
public class FlightBootstrapper implements CommandLineRunner {

    private final FlightService flightService;
    private final ScheduledFlightRepository flightRepository;

    public FlightBootstrapper(FlightService flightService, ScheduledFlightRepository flightRepository) {
        this.flightService = flightService;
        this.flightRepository = flightRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            if (flightRepository.count() > 0) {
                System.out.println("BOOTSTRAP: Flights already exist. Skipping.");
                return;
            }

            System.out.println("BOOTSTRAP WP2B: Scheduling realistic flight history...");

            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

            flightService.scheduleFlight(new CreateScheduledFlightDTO(
                    "RT-LISJFK", "CS-XPA", now.plusMinutes(5).toString()
            ));

            flightService.scheduleFlight(new CreateScheduledFlightDTO(
                    "RT-OPOLHR", "CS-BOA", now.plusMinutes(10).toString()
            ));

            flightService.scheduleFlight(new CreateScheduledFlightDTO(
                    "RT-OPOLIS", "CS-EMA", now.plusDays(2).withHour(8).toString()
            ));

            flightService.scheduleFlight(new CreateScheduledFlightDTO(
                    "RT-CDGJFK", "CS-XPB", now.plusDays(1).toString()
            ));

            Iterable<ScheduledFlight> flights = flightRepository.findAll();
            for (ScheduledFlight f : flights) {
                String routeId = f.getRoute().getRouteId().id();

                if (routeId.equals("RT-LISJFK")) {
                    f.updateStatus(FlightStatus.COMPLETED);
                } else if (routeId.equals("RT-OPOLHR")) {
                    f.updateStatus(FlightStatus.IN_FLIGHT);
                } else if (routeId.equals("RT-CDGJFK")) {
                    f.updateStatus(FlightStatus.CANCELLED);
                }

                flightRepository.save(f);
            }

            System.out.println("BOOTSTRAP: Flights successfully populated and statuses updated!");

        } catch (Exception e) {
            System.out.println("Note: Error creating flights in bootstrap: " + e.getMessage());
        }
    }
}