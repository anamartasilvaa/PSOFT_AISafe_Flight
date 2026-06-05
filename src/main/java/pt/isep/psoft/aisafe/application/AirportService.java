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

        // 👇 ADICIONA ESTAS 3 LINHAS (São elas que transferem a imagem do Postman para a Base de Dados) 👇
        if (dto.imageUrl() != null && !dto.imageUrl().isBlank()) {
            airport.updateImage(dto.imageUrl());
        }

        if (dto.facilities() != null) {
            dto.facilities().forEach(f ->
                    airport.addFacility(new Facility(f.type(), f.description()))
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
        Airport airport = airportRepository.findByIataCodeString(iataCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Airport not found"));

        ModelName modelNameVO = new ModelName(dto.modelName());

        AircraftModel model = aircraftModelRepository.findByModelName(modelNameVO)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + dto.modelName()));

        AirplaneCertification cert = new AirplaneCertification(
                dto.certificationNumber(),
                model,
                dto.issueDate(),
                dto.expiryDate()
        );

        airport.addOrUpdateAirplaneCertification(cert);
        Airport saved = airportRepository.save(airport);

        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AirportViewDTO> searchAirports(String name, String city, String country) {
        List<Airport> airports;

        if (name != null && !name.isBlank()) {
            airports = airportRepository.findByNameContainingIgnoreCase(name);
        } else if (city != null && !city.isBlank()) {
            airports = airportRepository.findByCityIgnoreCase(city);
        } else if (country != null && !country.isBlank()) {
            airports = airportRepository.findByCountryIgnoreCase(country);
        } else {
            airports = airportRepository.findAll();
        }

        return airports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // US109 - Update Status
    @Transactional
    public AirportViewDTO updateAirportStatus(String iataCode, String status) {
        Airport airport = airportRepository.findByIataCodeString(iataCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Airport not found"));

        try {
            AirportStatus newStatus = AirportStatus.valueOf(status.trim().toUpperCase());
            airport.updateStatus(newStatus);
            Airport saved = airportRepository.save(airport);
            return mapToDTO(saved);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Use: OPERATIONAL, CLOSED, or UNDER_MAINTENANCE");
        }
    }

    @Transactional
    public AirportViewDTO updateAirportImage(String iataCode, String imageUrl) {

        Airport airport = airportRepository.findByIataCodeString(iataCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + iataCode));

        airport.updateImage(imageUrl);
        airport = airportRepository.save(airport);
        return new AirportViewDTO(
                airport.getIataCode().code(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry(),
                airport.getTimezone(),
                airport.getType().toString(),
                airport.getStatus().toString(),
                airport.getRunways().stream()
                        .map(r -> new RunwayDTO(r.getName(), r.getLength(), r.getOrientation()))
                        .collect(Collectors.toList()),
                airport.getCertifications().stream()
                        .map(c -> new CertificationViewDTO(
                                c.getCertificationNumber(),
                                c.getAircraftModel().getModelName().name(),
                                c.getExpiryDate()))
                        .collect(Collectors.toList()),
                // Adiciona isto para completar os 11 argumentos:
                airport.getFacilities().stream()
                        .map(f -> new FacilityDTO(f.getType(), f.getDescription()))
                        .collect(Collectors.toList()),
                airport.getImageUrl()
        );
    }

    private AirportViewDTO mapToDTO(Airport airport) {
        return new AirportViewDTO(
                airport.getIataCode().code(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry(),
                airport.getTimezone(),
                airport.getType().toString(),
                airport.getStatus().toString(),
                airport.getRunways().stream()
                        .map(r -> new RunwayDTO(r.getName(), r.getLength(), r.getOrientation()))
                        .collect(Collectors.toList()),
                airport.getCertifications().stream()
                        .map(c -> new CertificationViewDTO(
                                c.getCertificationNumber(),
                                c.getAircraftModel().getModelName().name(),
                                c.getExpiryDate()))
                        .collect(Collectors.toList()),
                airport.getFacilities().stream()
                        .map(f -> new FacilityDTO(f.getType(), f.getDescription()))
                        .collect(Collectors.toList()),
                airport.getImageUrl()
        );
    }
    }
