package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ScheduledFlightService {

    private final ScheduledFlightRepository scheduledFlightRepository;
    private final RouteRepository routeRepository;
    private final AircraftRepository aircraftRepository;

    public ScheduledFlightService(ScheduledFlightRepository scheduledFlightRepository,
                                  RouteRepository routeRepository,
                                  AircraftRepository aircraftRepository) {
        this.scheduledFlightRepository = scheduledFlightRepository;
        this.routeRepository = routeRepository;
        this.aircraftRepository = aircraftRepository;
    }

    // --- US212: Agendar um Voo ---
    public ScheduledFlight scheduleFlight(CreateScheduledFlightDTO dto) {

        // 1. Converter e Validar a Data/Hora recebida do DTO
        LocalDateTime scheduledDate;
        try {
            scheduledDate = LocalDateTime.parse(dto.scheduledDateTime());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido. Usa o formato ISO: YYYY-MM-DDTHH:MM:SS");
        }

        // 2. Procurar a Rota na Base de Dados
        // Assumindo que o DTO envia a PK (Long) da rota. Se enviares a String do RouteId, o repositório terá de usar um findByRouteId.
        Route route = routeRepository.findById(Long.parseLong(dto.routeId()))
                .orElseThrow(() -> new IllegalArgumentException("Rota não encontrada com o ID: " + dto.routeId()));

        // 3. Procurar o Avião na Base de Dados
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(dto.registrationNumber()))
                .orElseThrow(() -> new IllegalArgumentException("Avião não encontrado com a matrícula: " + dto.registrationNumber()));

        // 4. Validação 1: O avião está ativo? (Pode estar em manutenção - cruza com o WP4)
        if (aircraft.getStatus() != AircraftStatus.ACTIVE) {
            throw new IllegalArgumentException("Não é possível agendar o voo. O estado atual do avião é: " + aircraft.getStatus());
        }

        // 5. Validação 2: Autonomia (Range requirement)
        Double routeMinimumRange = route.getMinimumRange();
        Double aircraftMaxRange = aircraft.getAircraftModel().getMaximumRange();

        if (routeMinimumRange > aircraftMaxRange) {
            throw new IllegalArgumentException(
                    "A autonomia máxima do avião (" + aircraftMaxRange + ") é insuficiente para a exigência da rota (" + routeMinimumRange + ")."
            );
        }

        // 6. Criar e guardar o Voo Agendado
        ScheduledFlight newFlight = new ScheduledFlight(route, aircraft, scheduledDate);

        return scheduledFlightRepository.save(newFlight);
    }

    // --- US213: Ver voos agendados por matrícula de avião ---
    public List<ScheduledFlight> getScheduledFlightsByAircraft(String registrationNumber) {
        return scheduledFlightRepository.findByAircraft_RegistrationNumber(new RegistrationNumber(registrationNumber));
    }
}