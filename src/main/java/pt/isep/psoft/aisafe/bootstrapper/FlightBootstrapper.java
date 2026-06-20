package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.ScheduledFlightService;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.domain.FlightStatus;
import pt.isep.psoft.aisafe.domain.ScheduledFlight;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Order(5)
public class FlightBootstrapper implements CommandLineRunner {

    private final ScheduledFlightService flightService;
    private final ScheduledFlightRepository flightRepository;

    public FlightBootstrapper(ScheduledFlightService flightService, ScheduledFlightRepository flightRepository) {
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

            // 1. Original flights for History (Past/Present)
            safeSchedule("RT-LISJFK", "CS-XPA", now.plusMinutes(5).toString());
            safeSchedule("RT-OPOLHR", "CS-BOA", now.plusMinutes(10).toString());
            safeSchedule("RT-OPOLIS", "CS-EMA", now.plusDays(2).withHour(8).toString());
            safeSchedule("RT-CDGJFK", "CS-XPB", now.plusDays(1).toString());

            // Future flights to test the Swap Algorithm (US222)
            System.out.println("BOOTSTRAP WP2B: Injecting future flights for Aircraft Swap testing...");

            // Aircraft CS-TPA with a full schedule in the upcoming days
            safeSchedule("RT-OPOLHR", "CS-TPA", now.plusDays(3).withHour(10).withMinute(0).toString());
            safeSchedule("RT-LISJFK", "CS-TPA", now.plusDays(4).withHour(14).withMinute(30).toString());

            // Aircraft CS-BOA with a scheduled flight for next week
            safeSchedule("RT-OPOLIS", "CS-BOA", now.plusDays(7).withHour(9).withMinute(0).toString());

            System.out.println("BOOTSTRAP WP2B: Attempting to schedule extra flights for the Top 5...");

            // Extra flights (Isolated to avoid breaking the Bootstrapper if validation fails)
            safeSchedule("RT-LHRJFK", "CS-TTB", now.plusDays(5).withHour(10).toString());
            safeSchedule("RT-LISCDG", "CS-TVC", now.plusDays(6).withHour(11).toString()); //

            // Status Update Logic
            Iterable<ScheduledFlight> flights = flightRepository.findAll();
            for (ScheduledFlight f : flights) {
                String routeId = f.getRoute().getRouteId().id();

                if (f.getScheduledDateTime().isBefore(now.plusHours(1))) {
                    if (routeId.equals("RT-LISJFK")) {
                        f.updateStatus(FlightStatus.COMPLETED);
                    } else if (routeId.equals("RT-OPOLHR")) {
                        f.updateStatus(FlightStatus.IN_FLIGHT);
                    } else if (routeId.equals("RT-CDGJFK")) {
                        f.updateStatus(FlightStatus.CANCELLED);
                    }
                }

                flightRepository.save(f);
            }

            System.out.println("BOOTSTRAP: Flights successfully populated and statuses updated!");

        } catch (Exception e) {
            System.out.println("Note: Error creating flights in bootstrap: " + e.getMessage());
        }
    }

    // --- HELPER METHOD TO ISOLATE ERRORS ---
    private void safeSchedule(String route, String aircraft, String date) {
        try {
            flightService.scheduleFlight(new CreateScheduledFlightDTO(route, aircraft, date));
            System.out.println(" -> Successfully scheduled flight: " + aircraft + " on route " + route);
        } catch (Exception e) {
            System.out.println(" -> WARNING (Ignored): Failed to schedule " + aircraft + " on route " + route + " | Reason: " + e.getMessage());
        }
    }
}