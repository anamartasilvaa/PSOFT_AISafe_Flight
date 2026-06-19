package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AirportRepository;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import java.util.List;
import java.util.Map;
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
    public AirportViewDTO updateAirportImage(String iataCode, org.springframework.web.multipart.MultipartFile file) {

        Airport airport = airportRepository.findByIataCodeString(iataCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + iataCode));

        try {
            String projectDirectory = System.getProperty("user.dir");
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(projectDirectory, "uploads");

            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            String fileName = iataCode.toUpperCase() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = uploadPath.resolve(fileName);

            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/" + fileName;
            airport.updateImage(fileUrl);

            airport = airportRepository.save(airport);

            return mapToDTO(airport);

        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to save airport image: " + e.getMessage());
        }
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
                airport.getImageUrl(),
                airport.getOperationalHours(),
                airport.getContactInformation()
        );
    }

    @Transactional
    public AirportViewDTO updateAirportDetails(String iataCode, UpdateAirportDetailsDTO dto) {
        Airport airport = airportRepository.findByIataCodeString(iataCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Airport not found: " + iataCode));

        airport.updateDetails(dto.operationalHours(), dto.contactInformation());
        airport = airportRepository.save(airport);

        return mapToDTO(airport);
    }

    //  US211: View airports grouped by region or country
    @Transactional(readOnly = true)
    public Object getAirportsGrouped(String groupBy) {
        List<Airport> airports = airportRepository.findAll();

        if ("both".equalsIgnoreCase(groupBy)) {
            return airports.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.groupingBy(
                            this::extractRegion,
                            Collectors.groupingBy(dto -> dto.country() != null ? dto.country() : "Unknown Country")
                    ));
        }

        if ("region".equalsIgnoreCase(groupBy)) {
            return airports.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.groupingBy(this::extractRegion));
        }

        return airports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.groupingBy(dto -> dto.country() != null ? dto.country() : "Unknown Country"));
    }

    private String extractRegion(AirportViewDTO dto) {
        if (dto.timezone() != null && dto.timezone().contains("/")) {
            return dto.timezone().split("/")[0];
        }
        return "Unknown Region";
    }

    // US225
    @Transactional
    public List<AirportViewDTO> importAirportsFromCsv(org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded CSV file is empty.");
        }

        List<AirportViewDTO> importedAirports = new java.util.ArrayList<>();

        try (java.io.BufferedReader fileReader = new java.io.BufferedReader(new java.io.InputStreamReader(file.getInputStream(), "UTF-8"));
             org.apache.commons.csv.CSVParser csvParser = new org.apache.commons.csv.CSVParser(fileReader,
                     org.apache.commons.csv.CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (org.apache.commons.csv.CSVRecord csvRecord : csvParser) {
                RegisterAirportDTO dto = new RegisterAirportDTO(
                        csvRecord.get("iataCode"),
                        csvRecord.get("name"),
                        csvRecord.get("city"),
                        csvRecord.get("country"),
                        csvRecord.get("timezone"),
                        csvRecord.get("type"),
                        Double.parseDouble(csvRecord.get("latitude")),
                        Double.parseDouble(csvRecord.get("longitude")),
                        List.of(), // runways
                        List.of(), // facilities
                        null       // imageUrl
                );

                AirportViewDTO savedAirport = registerAirport(dto);
                importedAirports.add(savedAirport);
            }

            return importedAirports;

        } catch (java.io.IOException e) {
            throw new RuntimeException("Error parsing the CSV file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error in CSV formatting/data: " + e.getMessage());
        }
    }
    }
