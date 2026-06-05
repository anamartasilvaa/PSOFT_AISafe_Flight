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

    public List<TopAircraftModelDTO> getTop5UtilizedModels() {
        // Pede a página 0 com o limite de 5 resultados
        return aircraftRepository.findTop5UtilizedModels(org.springframework.data.domain.PageRequest.of(0, 5));
    }

    public List<RouteViewDTO> getCompatibleRoutesForAircraft(String regNum) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        Double maxRange = aircraft.getAircraftModel().getMaximumRange();
        Integer actualCapacity = aircraft.getActualSeatingCapacity();

        org.springframework.data.domain.Page<Route> compatibleRoutes =
                routeRepository.findCompatibleRoutes(maxRange, actualCapacity, org.springframework.data.domain.PageRequest.of(0, 20));

        // Converter cada Entidade Route para o teu RouteViewDTO
        return compatibleRoutes.stream()
                .map(route -> new RouteViewDTO(
                        route.getRouteId().toString(),
                        route.getOrigin().getIataCode().toString(), // Ajusta aqui se o teu getIataCode() for diferente
                        route.getDestination().getIataCode().toString(),
                        route.getStatus().name(),
                        route.getMinimumCapacity()
                ))
                .toList();
    }

    public String getRealTimeAircraftStatus(String regNum) {
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum.trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        // 1. Verificar o estado base do avião
        if (aircraft.getStatus() == AircraftStatus.UNDER_MAINTENANCE) {
            return "under maintenance";
        }
        if (aircraft.getStatus() == AircraftStatus.INACTIVE) {
            return "inactive";
        }

        // 2. Se está ACTIVE, vamos ver se tem algum voo a decorrer AGORA
        boolean isFlying = scheduledFlightRepository.existsByAircraftAndStatus(aircraft, FlightStatus.IN_FLIGHT);

        if (isFlying) {
            return "in-flight";
        }

        // 3. Se está ACTIVE e não está a voar, então está disponível!
        return "available";
    }
}