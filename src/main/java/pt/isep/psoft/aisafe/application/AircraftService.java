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
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path uploadPath = Paths.get("uploads/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error saving image " + e.getMessage());
        }
    }

    @Transactional
    public void registerModel(RegisterAircraftModelDTO dto) {
        ModelName name = new ModelName(dto.modelName());
        Manufacturer manufacturer = Manufacturer.valueOf(dto.manufacturer().toUpperCase());

        AircraftModel model = new AircraftModel(
                name, manufacturer, dto.seatingCapacity(), dto.fuelCapacity(),
                dto.range(), dto.speed(), dto.modelPhotoUrl(), null, null
        );

        modelRepository.save(model);
    }

    @Transactional
    public void registerAircraft(RegisterAircraftDTO dto) {
        AircraftModel model = modelRepository.findByModelName(new ModelName(dto.modelName()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + dto.modelName()));

        Aircraft aircraft = new Aircraft(
                new RegistrationNumber(dto.registrationNumber()),
                model,
                dto.manufacturingDate(),
                dto.seatingCapacity()
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

        if (year != null) {
            results = aircraftRepository.findByManufacturingYear(year);
        } else if (status != null && !status.isBlank()) {
            results = aircraftRepository.findByStatus(AircraftStatus.valueOf(status.toUpperCase()));
        } else if (model != null && !model.isBlank()) {
            results = aircraftRepository.findByModel_ModelName(new ModelName(model));
        } else {
            results = aircraftRepository.findAll();
        }

        return results.stream()
                .map(this::mapToViewDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AircraftViewDTO updateAircraftStatus(String regNum, UpdateAircraftStatusDTO dto) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        AircraftStatus newStatus = AircraftStatus.valueOf(dto.status().toUpperCase());
        aircraft.updateStatus(newStatus);
        aircraftRepository.save(aircraft);

        if (newStatus == AircraftStatus.UNDER_MAINTENANCE || newStatus == AircraftStatus.INACTIVE) {
            handleAircraftSwap(aircraft);
        }

        return mapToViewDTO(aircraft);
    }

    // --- NOVO MÉTODO: Devolve o relatório para o Postman ---
    @Transactional
    public Map<String, Object> updateAircraftStatusWithReport(String regNum, UpdateAircraftStatusDTO dto) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        AircraftStatus newStatus = AircraftStatus.valueOf(dto.status().toUpperCase());
        aircraft.updateStatus(newStatus);
        aircraftRepository.save(aircraft);

        List<String> swapLogs = new ArrayList<>();
        if (newStatus == AircraftStatus.UNDER_MAINTENANCE || newStatus == AircraftStatus.INACTIVE) {
            swapLogs = handleAircraftSwap(aircraft);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("aircraft", mapToViewDTO(aircraft));
        response.put("swapReport", swapLogs);

        return response;
    }

    // ALGORITMO DE RELATÓRIO
    private List<String> handleAircraftSwap(Aircraft groundedAircraft) {
        List<String> report = new ArrayList<>();
        report.add("Aircraft " + groundedAircraft.getRegistrationNumber().number() + " grounded. Scanning for affected flights...");
        System.out.println(report.get(0));

        List<ScheduledFlight> allFlights = scheduledFlightRepository.findByAircraft_RegistrationNumber(groundedAircraft.getRegistrationNumber());

        List<ScheduledFlight> affectedFlights = allFlights.stream()
                .filter(f -> f.getStatus() == FlightStatus.SCHEDULED && f.getScheduledDateTime().isAfter(java.time.LocalDateTime.now()))
                .collect(Collectors.toList());

        if (affectedFlights.isEmpty()) {
            String msg = "No future flights affected. Safe to ground.";
            report.add(msg);
            System.out.println(msg);
            return report;
        }

        List<Aircraft> swapCandidates = aircraftRepository.findAll().stream()
                .filter(a -> a.getAircraftModel().equals(groundedAircraft.getAircraftModel()))
                .filter(a -> a.getStatus() == AircraftStatus.ACTIVE)
                .filter(a -> !a.equals(groundedAircraft))
                .collect(Collectors.toList());

        for (ScheduledFlight flight : affectedFlights) {
            boolean isSwapped = false;

            for (Aircraft candidate : swapCandidates) {
                if (isAircraftFree(candidate, flight.getScheduledDateTime())) {
                    String msg = "RECOVERED: Flight on " + flight.getScheduledDateTime() +
                            " reassigned from " + groundedAircraft.getRegistrationNumber().number() +
                            " to " + candidate.getRegistrationNumber().number();
                    report.add(msg);
                    System.out.println(msg);

                    flight.changeAircraft(candidate);
                    scheduledFlightRepository.save(flight);
                    isSwapped = true;
                    break;
                }
            }

            if (!isSwapped) {
                String msg = "ALERT: No replacement found! Flight on " + flight.getScheduledDateTime() + " is CANCELLED.";
                report.add(msg);
                System.out.println(msg);
                flight.updateStatus(FlightStatus.CANCELLED);
                scheduledFlightRepository.save(flight);
            }
        }
        report.add("Swap Operation complete.");
        return report;
    }

    private boolean isAircraftFree(Aircraft candidate, java.time.LocalDateTime targetTime) {
        List<ScheduledFlight> candidateFlights = scheduledFlightRepository.findByAircraft_RegistrationNumber(candidate.getRegistrationNumber());

        for (ScheduledFlight f : candidateFlights) {
            if (f.getStatus() == FlightStatus.SCHEDULED || f.getStatus() == FlightStatus.IN_FLIGHT) {
                java.time.LocalDateTime flightStartBuffer = f.getScheduledDateTime().minusHours(4);
                java.time.LocalDateTime flightEndBuffer = f.getScheduledDateTime().plusHours(4);

                if (targetTime.isAfter(flightStartBuffer) && targetTime.isBefore(flightEndBuffer)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Transactional
    public AircraftModelViewDTO updateModelSpecifications(String modelName, UpdateAircraftModelSpecsDTO dto) {
        ModelName nameVO = new ModelName(modelName.trim().toUpperCase());

        AircraftModel model = modelRepository.findByModelName(nameVO)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + modelName));

        model.updateSpecifications(
                dto.seatingCapacity(),
                dto.fuelCapacity(),
                dto.maximumRange(),
                dto.cruisingSpeed(),
                dto.seatingConfiguration(),
                dto.operatingHoursRange()
        );

        AircraftModel savedModel = modelRepository.save(model);
        return new AircraftModelViewDTO(
                savedModel.getModelName().name(),
                savedModel.getManufacturer().name(),
                savedModel.getDefaultSeatingCapacity(),
                savedModel.getFuelCapacity(),
                savedModel.getMaximumRange(),
                savedModel.getCruisingSpeed(),
                savedModel.getModelPhotoUrl(),
                savedModel.getSeatingConfiguration(),
                savedModel.getOperatingHoursRange()
        );
    }

    @Transactional
    public AircraftModelViewDTO updateModelImage(String modelName, MultipartFile file) {
        AircraftModel model = modelRepository.findByModelName(new ModelName(modelName.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + modelName));

        String savedImagePath = saveImageLocally(file);
        if (savedImagePath == null) {
            throw new IllegalArgumentException("The image file cannot be empty");
        }

        model.updateImage(savedImagePath);
        AircraftModel savedModel = modelRepository.save(model);

        return new AircraftModelViewDTO(
                savedModel.getModelName().name(), savedModel.getManufacturer().name(),
                savedModel.getDefaultSeatingCapacity(), savedModel.getFuelCapacity(),
                savedModel.getMaximumRange(), savedModel.getCruisingSpeed(),
                savedModel.getModelPhotoUrl(), savedModel.getSeatingConfiguration(),
                savedModel.getOperatingHoursRange()
        );
    }

    private AircraftViewDTO mapToViewDTO(Aircraft aircraft) {
        return new AircraftViewDTO(
                aircraft.getRegistrationNumber().toString(),
                aircraft.getAircraftModel().getModelName().name(),
                aircraft.getManufacturingDate(),
                aircraft.getActualSeatingCapacity(),
                aircraft.getStatus().toString(),
                aircraft.getAircraftModel().getModelPhotoUrl()
        );
    }

    public OperationalHoursDTO getAircraftOperationalHours(String regNum) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        return new OperationalHoursDTO(
                aircraft.getRegistrationNumber().number(),
                aircraft.getTotalFlightHours()
        );
    }

    // US206
    public org.springframework.data.domain.Page<OperationalHoursDTO> getAllAircraftOperationalHours(
            org.springframework.data.domain.Pageable pageable) {

        return aircraftRepository.findAll(pageable)
                .map(aircraft -> new OperationalHoursDTO(
                        aircraft.getRegistrationNumber().number(),
                        aircraft.getTotalFlightHours()
                ));
    }

    // US204
    public List<TopAircraftModelDTO> getTop5UtilizedModels(String sortBy) {
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 5);

        if ("assignments".equalsIgnoreCase(sortBy)) {
            return aircraftRepository.findTop5ModelsByAssignments(pageRequest);
        }

        return aircraftRepository.findTop5ModelsByFlightHours(pageRequest);
    }

    public org.springframework.data.domain.Page<RouteViewDTO> getCompatibleRoutesForAircraft(
            String regNum, org.springframework.data.domain.Pageable pageable) {

        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        Double maxRange = aircraft.getAircraftModel().getMaximumRange();
        Integer actualCapacity = aircraft.getActualSeatingCapacity();

        org.springframework.data.domain.Page<Route> compatibleRoutes =
                routeRepository.findCompatibleRoutes(maxRange, actualCapacity, pageable);

        return compatibleRoutes.map(route -> new RouteViewDTO(
                route.getRouteId().toString(),
                route.getOrigin().getIataCode().code(),
                route.getDestination().getIataCode().code(),
                route.getStatus().name(),
                route.getMinimumCapacity()
        ));
    }

    public String getRealTimeAircraftStatus(String regNum) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        if (aircraft.getStatus() == AircraftStatus.UNDER_MAINTENANCE) {
            return "under maintenance";
        }
        if (aircraft.getStatus() == AircraftStatus.INACTIVE) {
            return "inactive";
        }

        boolean isFlying = scheduledFlightRepository.existsByAircraftAndStatus(aircraft, FlightStatus.IN_FLIGHT);

        if (isFlying) {
            return "in-flight";
        }

        return "available";
    }
}