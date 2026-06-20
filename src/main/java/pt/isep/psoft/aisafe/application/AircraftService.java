package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

@Service
public class AircraftService {

    private final AircraftModelRepository modelRepository;
    private final AircraftRepository aircraftRepository;
    private final RouteRepository routeRepository;
    private final ScheduledFlightRepository scheduledFlightRepository;

    public AircraftService(AircraftModelRepository modelRepository,
                           AircraftRepository aircraftRepository,
                           RouteRepository routeRepository,
                           ScheduledFlightRepository scheduledFlightRepository) {
        this.modelRepository = modelRepository;
        this.aircraftRepository = aircraftRepository;
        this.routeRepository = routeRepository;
        this.scheduledFlightRepository = scheduledFlightRepository;
    }

    private String saveImageLocally(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path uploadPath = Paths.get("uploads/");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error saving image " + e.getMessage());
        }
    }

    @Transactional
    public void registerModel(RegisterAircraftModelDTO dto) {
        AircraftModel model = new AircraftModel(
                new ModelName(dto.modelName()), Manufacturer.valueOf(dto.manufacturer().toUpperCase()),
                dto.seatingCapacity(), dto.fuelCapacity(), dto.range(), dto.speed(),
                dto.modelPhotoUrl(), null, null, dto.engineType()
        );
        modelRepository.save(model);
    }

    @Transactional
    public void registerAircraft(RegisterAircraftDTO dto) {
        AircraftModel model = modelRepository.findByModelName(new ModelName(dto.modelName()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + dto.modelName()));
        Aircraft aircraft = new Aircraft(
                new RegistrationNumber(dto.registrationNumber()), model,
                dto.manufacturingDate(), dto.seatingCapacity(), dto.features()
        );
        aircraftRepository.save(aircraft);
    }

    public AircraftViewDTO getAircraftByRegistrationNumber(String regNum) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));
        return mapToViewDTO(aircraft);
    }

    public List<AircraftViewDTO> searchAircrafts(String model, String status, Integer year) {
        List<Aircraft> results;
        if (year != null) results = aircraftRepository.findByManufacturingYear(year);
        else if (status != null && !status.isBlank()) results = aircraftRepository.findByStatus(AircraftStatus.valueOf(status.toUpperCase()));
        else if (model != null && !model.isBlank()) results = aircraftRepository.findByModel_ModelName(new ModelName(model));
        else results = aircraftRepository.findAll();
        return results.stream().map(this::mapToViewDTO).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> updateAircraftStatusWithReport(String regNum, UpdateAircraftStatusDTO dto) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));
        aircraft.updateStatus(AircraftStatus.valueOf(dto.status().toUpperCase()));
        aircraftRepository.save(aircraft);
        List<String> swapLogs = (aircraft.getStatus() == AircraftStatus.UNDER_MAINTENANCE || aircraft.getStatus() == AircraftStatus.INACTIVE)
                ? handleAircraftSwap(aircraft) : new ArrayList<>();
        Map<String, Object> response = new HashMap<>();
        response.put("aircraft", mapToViewDTO(aircraft));
        response.put("swapReport", swapLogs);
        return response;
    }

    private List<String> handleAircraftSwap(Aircraft groundedAircraft) {
        List<String> report = new ArrayList<>();
        List<ScheduledFlight> affectedFlights = scheduledFlightRepository.findByAircraft_RegistrationNumber(groundedAircraft.getRegistrationNumber()).stream()
                .filter(f -> f.getStatus() == FlightStatus.SCHEDULED && f.getScheduledDateTime().isAfter(java.time.LocalDateTime.now()))
                .collect(Collectors.toList());

        List<Aircraft> swapCandidates = aircraftRepository.findAll().stream()
                .filter(a -> a.getAircraftModel().equals(groundedAircraft.getAircraftModel()) && a.getStatus() == AircraftStatus.ACTIVE && !a.equals(groundedAircraft))
                .collect(Collectors.toList());

        for (ScheduledFlight flight : affectedFlights) {
            boolean isSwapped = false;
            for (Aircraft candidate : swapCandidates) {
                if (isAircraftFree(candidate, flight.getScheduledDateTime())) {
                    report.add("RECOVERED: Flight on " + flight.getScheduledDateTime() + " reassigned to " + candidate.getRegistrationNumber().number());
                    flight.changeAircraft(candidate);
                    scheduledFlightRepository.save(flight);
                    isSwapped = true;
                    break;
                }
            }
            if (!isSwapped) {
                report.add("ALERT: No replacement found! Flight on " + flight.getScheduledDateTime() + " is CANCELLED.");
                flight.updateStatus(FlightStatus.CANCELLED);
                scheduledFlightRepository.save(flight);
            }
        }
        return report;
    }

    private boolean isAircraftFree(Aircraft candidate, java.time.LocalDateTime targetTime) {
        return scheduledFlightRepository.findByAircraft_RegistrationNumber(candidate.getRegistrationNumber()).stream()
                .filter(f -> f.getStatus() == FlightStatus.SCHEDULED || f.getStatus() == FlightStatus.IN_FLIGHT)
                .noneMatch(f -> targetTime.isAfter(f.getScheduledDateTime().minusHours(4)) && targetTime.isBefore(f.getScheduledDateTime().plusHours(4)));
    }

    @Transactional
    public AircraftModelViewDTO updateModelSpecifications(String modelName, UpdateAircraftModelSpecsDTO dto) {
        AircraftModel model = modelRepository.findByModelName(new ModelName(modelName.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + modelName));
        model.updateSpecifications(dto.seatingCapacity(), dto.fuelCapacity(), dto.maximumRange(), dto.cruisingSpeed(), dto.seatingConfiguration(), dto.operatingHoursRange());
        AircraftModel savedModel = modelRepository.save(model);
        return new AircraftModelViewDTO(savedModel.getModelName().name(), savedModel.getManufacturer().name(), savedModel.getDefaultSeatingCapacity(), savedModel.getFuelCapacity(), savedModel.getMaximumRange(), savedModel.getCruisingSpeed(), savedModel.getModelPhotoUrl(), savedModel.getSeatingConfiguration(), savedModel.getOperatingHoursRange(), savedModel.getEngineType());
    }

    @Transactional
    public AircraftModelViewDTO updateModelImage(String modelName, MultipartFile file) {
        AircraftModel model = modelRepository.findByModelName(new ModelName(modelName.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + modelName));
        model.updateImage(saveImageLocally(file));
        AircraftModel savedModel = modelRepository.save(model);
        return new AircraftModelViewDTO(savedModel.getModelName().name(), savedModel.getManufacturer().name(), savedModel.getDefaultSeatingCapacity(), savedModel.getFuelCapacity(), savedModel.getMaximumRange(), savedModel.getCruisingSpeed(), savedModel.getModelPhotoUrl(), savedModel.getSeatingConfiguration(), savedModel.getOperatingHoursRange(), savedModel.getEngineType());
    }

    private AircraftViewDTO mapToViewDTO(Aircraft aircraft) {
        return new AircraftViewDTO(
                aircraft.getRegistrationNumber().toString(),
                aircraft.getAircraftModel().getModelName().name(),
                aircraft.getManufacturingDate(),
                aircraft.getActualSeatingCapacity(),
                aircraft.getStatus().toString(),
                aircraft.getAircraftModel().getModelPhotoUrl(),
                aircraft.getFeatures(),
                aircraft.getAircraftModel().getEngineType()
        );
    }

    public OperationalHoursDTO getAircraftOperationalHours(String regNum) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));
        return new OperationalHoursDTO(aircraft.getRegistrationNumber().number(), aircraft.getTotalFlightHours());
    }

    public org.springframework.data.domain.Page<OperationalHoursDTO> getAllAircraftOperationalHours(org.springframework.data.domain.Pageable pageable) {
        return aircraftRepository.findAll(pageable)
                .map(aircraft -> new OperationalHoursDTO(aircraft.getRegistrationNumber().number(), aircraft.getTotalFlightHours()));
    }

    public List<TopAircraftModelDTO> getTop5UtilizedModels(String sortBy) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 5);
        return "assignments".equalsIgnoreCase(sortBy) ? aircraftRepository.findTop5ModelsByAssignments(pageRequest) : aircraftRepository.findTop5ModelsByFlightHours(pageRequest);
    }

    public org.springframework.data.domain.Page<RouteViewDTO> getCompatibleRoutesForAircraft(String regNum, org.springframework.data.domain.Pageable pageable) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));
        return routeRepository.findCompatibleRoutes(aircraft.getAircraftModel().getMaximumRange(), aircraft.getActualSeatingCapacity(), pageable)
                .map(route -> new RouteViewDTO(route.getRouteId().toString(), route.getOrigin().getIataCode().code(), route.getDestination().getIataCode().code(), route.getStatus().name(), route.getMinimumCapacity()));
    }

    public List<AircraftViewDTO> searchAircraftByFeatures(String feature, String engineType) {
        return aircraftRepository.findByFeaturesAndEngine(
                (feature == null || feature.trim().isEmpty()) ? null : feature,
                (engineType == null || engineType.trim().isEmpty()) ? null : engineType
        ).stream().map(this::mapToViewDTO).collect(Collectors.toList());
    }

    public String getRealTimeAircraftStatus(String regNum) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));
        if (aircraft.getStatus() == AircraftStatus.UNDER_MAINTENANCE) return "under maintenance";
        if (aircraft.getStatus() == AircraftStatus.INACTIVE) return "inactive";
        return scheduledFlightRepository.existsByAircraftAndStatus(aircraft, FlightStatus.IN_FLIGHT) ? "in-flight" : "available";
    }
}