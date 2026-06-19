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
public class ScheduledFlightService {

    private final ScheduledFlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final AircraftRepository aircraftRepository;

    public ScheduledFlightService(ScheduledFlightRepository flightRepository,
                                  RouteRepository routeRepository,
                                  AircraftRepository aircraftRepository) {
        this.flightRepository = flightRepository;
        this.routeRepository = routeRepository;
        this.aircraftRepository = aircraftRepository;
    }

    @Transactional
    public ScheduledFlightViewDTO scheduleFlight(CreateScheduledFlightDTO dto) {
        // Usa RouteId (Value Object) corretamente
        Route route = routeRepository.findByRouteId(new RouteId(dto.routeId()))
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + dto.routeId()));

        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(dto.registrationNumber()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + dto.registrationNumber()));

        // Validações de Negócio Robustas
        if (aircraft.getStatus() != AircraftStatus.ACTIVE) {
            throw new IllegalStateException("Aircraft is not available. Status: " + aircraft.getStatus());
        }
        if (route.getOrigin().getStatus() != AirportStatus.OPERATIONAL ||
                route.getDestination().getStatus() != AirportStatus.OPERATIONAL) {
            throw new IllegalStateException("One of the airports is not operational.");
        }
        if (aircraft.getActualSeatingCapacity() < route.getMinimumCapacity() ||
                aircraft.getAircraftModel().getMaximumRange() < route.getMinimumRange()) {
            throw new IllegalArgumentException("Aircraft does not meet route requirements.");
        }

        LocalDateTime dateTime = LocalDateTime.parse(dto.scheduledDateTime());
        ScheduledFlight flight = new ScheduledFlight(route, aircraft, dateTime);
        ScheduledFlight savedFlight = flightRepository.save(flight);

        return new ScheduledFlightViewDTO(
                savedFlight.getPk(),
                savedFlight.getRoute().getRouteId().id(),
                savedFlight.getAircraft().getRegistrationNumber().number(),
                savedFlight.getScheduledDateTime().toString(),
                savedFlight.getStatus().name()
        );
    }

    public List<ScheduledFlightViewDTO> getScheduledFlightsByAircraft(String registrationNumber) {
        RegistrationNumber regNum = new RegistrationNumber(registrationNumber.trim().toUpperCase());
        return flightRepository.findByAircraft_RegistrationNumber(regNum).stream()
                .map(f -> new ScheduledFlightViewDTO(
                        f.getPk(),
                        f.getRoute().getRouteId().id(),
                        f.getAircraft().getRegistrationNumber().number(),
                        f.getScheduledDateTime().toString(),
                        f.getStatus().name()))
                .toList();
    }
}