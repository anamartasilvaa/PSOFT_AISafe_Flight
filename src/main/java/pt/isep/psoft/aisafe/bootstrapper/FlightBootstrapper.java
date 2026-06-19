package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.ScheduledFlightService; // Import corrigido
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

            // 1. Voos originais para Histórico (Passado/Presente)
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

            // Voos Futuros para testar o Algoritmo de Swap (US222)
            System.out.println("BOOTSTRAP WP2B: Injecting future flights for Aircraft Swap testing...");

            // Avião CS-TPA com a agenda cheia nos próximos dias
            flightService.scheduleFlight(new CreateScheduledFlightDTO(
                    "RT-OPOLHR", "CS-TPA", now.plusDays(3).withHour(10).withMinute(0).toString()
            ));

            flightService.scheduleFlight(new CreateScheduledFlightDTO(
                    "RT-LISJFK", "CS-TPA", now.plusDays(4).withHour(14).withMinute(30).toString()
            ));

            // Avião CS-BOA com um voo marcado para a próxima semana
            flightService.scheduleFlight(new CreateScheduledFlightDTO(
                    "RT-OPOLIS", "CS-BOA", now.plusDays(7).withHour(9).withMinute(0).toString()
            ));

            // -------------------------------------------------------------------------

            // Atualização de Estados
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
}