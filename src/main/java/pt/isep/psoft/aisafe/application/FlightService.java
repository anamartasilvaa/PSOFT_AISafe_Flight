package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.application.DTO.ScheduledFlightViewDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightService {

    private final ScheduledFlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final AircraftRepository aircraftRepository;

    public FlightService(ScheduledFlightRepository flightRepository,
                         RouteRepository routeRepository,
                         AircraftRepository aircraftRepository) {
        this.flightRepository = flightRepository;
        this.routeRepository = routeRepository;
        this.aircraftRepository = aircraftRepository;
    }

    // US212 - Assign an aircraft to a route for a specific date and time
    @Transactional
    public ScheduledFlightViewDTO scheduleFlight(CreateScheduledFlightDTO dto) {

        // 1. Procurar a Rota
        Route route = routeRepository.findByRouteId(new RouteId(dto.routeId()))
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + dto.routeId()));

        // 2. Procurar o Avião
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(dto.registrationNumber()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + dto.registrationNumber()));

        // 3. Validação de Disponibilidade (Status)
        if (aircraft.getStatus() != AircraftStatus.ACTIVE) {
            throw new IllegalStateException("Aircraft is not available for flights. Current status: " + aircraft.getStatus());
        }
        if (route.getOrigin().getStatus() != AirportStatus.OPERATIONAL) {
            throw new IllegalStateException("Origin airport is not operational.");
        }
        if (route.getDestination().getStatus() != AirportStatus.OPERATIONAL) {
            throw new IllegalStateException("Destination airport is not operational.");
        }

        // 4. Validação de Capacidade de Passageiros
        if (aircraft.getActualSeatingCapacity() < route.getMinimumCapacity()) {
            throw new IllegalArgumentException("Aircraft capacity is insufficient for this route's minimum requirements.");
        }

        // 5. Validação de Alcance (Range)
        if (aircraft.getAircraftModel().getMaximumRange() < route.getMinimumRange()) {
            throw new IllegalArgumentException("Aircraft maximum range is insufficient for this route.");
        }

        // 6. Criar e Gravar o Voo
        LocalDateTime dateTime = LocalDateTime.parse(dto.scheduledDateTime());
        ScheduledFlight flight = new ScheduledFlight(route, aircraft, dateTime);
        ScheduledFlight savedFlight = flightRepository.save(flight);

        // 7. Mapear para DTO de Saída
        return new ScheduledFlightViewDTO(
                savedFlight.getPk(),
                savedFlight.getRoute().getRouteId().id(),
                savedFlight.getAircraft().getRegistrationNumber().number(),
                savedFlight.getScheduledDateTime().toString(),
                savedFlight.getStatus().name()
        );
    }

    // US213 - View all scheduled flights for a specific aircraft
    public List<ScheduledFlightViewDTO> getScheduledFlightsByAircraft(String registrationNumber) {
        RegistrationNumber regNum = new RegistrationNumber(registrationNumber.trim().toUpperCase());

        List<ScheduledFlight> flights = flightRepository.findByAircraft_RegistrationNumber(regNum);

        return flights.stream().map(flight -> new ScheduledFlightViewDTO(
                flight.getPk(),
                flight.getRoute().getRouteId().id(),
                flight.getAircraft().getRegistrationNumber().number(),
                flight.getScheduledDateTime().toString(),
                flight.getStatus().name()
        )).toList();
    }
}