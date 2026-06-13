package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.DTO.*;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.util.List;
import java.util.stream.Collectors;

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

        aircraft.updateStatus(AircraftStatus.valueOf(dto.status().toUpperCase()));
        aircraftRepository.save(aircraft);

        return mapToViewDTO(aircraft);
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
    public AircraftModelViewDTO updateModelImage(String modelName, String imageUrl) {
        AircraftModel model = modelRepository.findByModelName(new ModelName(modelName.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + modelName));

        model.updateImage(imageUrl);
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
                route.getOrigin().getIataCode().toString(),
                route.getDestination().getIataCode().toString(),
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