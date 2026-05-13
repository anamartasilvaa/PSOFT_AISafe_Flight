package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AirportRepository;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AirportService {
    private final AirportRepository airportRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public AirportService(AirportRepository airportRepository, AircraftModelRepository aircraftModelRepository) {
        this.airportRepository = airportRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    @Transactional
    public AirportViewDTO registerAirport(RegisterAirportDTO dto) {
        Airport airport = new Airport(
                new IATACode(dto.iataCode()),
                dto.name(),
                dto.city(),
                dto.country(),
                dto.timezone(),
                AirportType.valueOf(dto.type().toUpperCase()),
                new Coordinates(dto.latitude(), dto.longitude())
        );

        if (dto.runways() != null) {
            dto.runways().forEach(r ->
                    airport.addRunway(new Runway(r.name(), r.length(), r.orientation()))
            );
        }

        Airport saved = airportRepository.save(airport);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public AirportViewDTO getAirportByIataCode(String iataCode) {
        String cleanCode = iataCode.trim().toUpperCase();
        Airport airport = airportRepository.findByIataCodeString(cleanCode)
                .orElseThrow(() -> new IllegalArgumentException("Airport not found with IATA Code: " + cleanCode));

        return mapToDTO(airport);
    }

    @Transactional
    public AirportViewDTO addCertification(String iataCode, AddCertificationDTO dto) {
        // 1. Procurar o aeroporto
        Airport airport = airportRepository.findByIataCodeString(iataCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Airport not found"));

        // 2. RESPEITO AO MD: Criar o Value Object ModelName para a pesquisa
        ModelName modelNameVO = new ModelName(dto.modelName());

        // 3. Procurar o modelo usando o Value Object no repositório original
        AircraftModel model = aircraftModelRepository.findByModelName(modelNameVO)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + dto.modelName()));

        // 4. Criar a certificação
        AirplaneCertification cert = new AirplaneCertification(
                dto.certificationNumber(),
                model,
                dto.issueDate(),
                dto.expiryDate()
        );

        // 5. Adicionar ao aeroporto e guardar
        airport.addAirplaneCertification(cert);
        Airport saved = airportRepository.save(airport);

        return mapToDTO(saved);
    }

    private AirportViewDTO mapToDTO(Airport airport) {
        return new AirportViewDTO(
                airport.getIataCode().code(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry(),
                airport.getTimezone(),
                airport.getType().toString(),
                airport.getRunways().stream()
                        .map(r -> new RunwayDTO(r.getName(), r.getLength(), r.getOrientation()))
                        .collect(Collectors.toList())
        );
    }
}