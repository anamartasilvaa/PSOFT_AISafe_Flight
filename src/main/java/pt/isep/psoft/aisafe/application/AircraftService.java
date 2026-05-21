package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftModelDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.application.DTO.AircraftViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAircraftStatusDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AircraftService {

    private final AircraftModelRepository modelRepository;
    private final AircraftRepository aircraftRepository;

    public AircraftService(AircraftModelRepository modelRepository, AircraftRepository aircraftRepository) {
        this.modelRepository = modelRepository;
        this.aircraftRepository = aircraftRepository;
    }

    @Transactional
    public void registerModel(RegisterAircraftModelDTO dto) {
        ModelName name = new ModelName(dto.modelName());
        Manufacturer manufacturer = Manufacturer.valueOf(dto.manufacturer().toUpperCase());

        AircraftModel model = new AircraftModel(
                name, manufacturer, dto.seatingCapacity(), dto.fuelCapacity(),
                dto.range(), dto.speed(), dto.modelPhotoUrl()
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

        return new AircraftViewDTO(
                aircraft.getRegistrationNumber().toString(),
                aircraft.getAircraftModel().getModelName().toString(),
                aircraft.getManufacturingDate(),
                aircraft.getActualSeatingCapacity(),
                aircraft.getStatus().toString(),
                aircraft.getAircraftModel().getModelPhotoUrl()
        );
    }

    // --- AQUI ESTÁ O MÉTODO ATUALIZADO COM A PAGINAÇÃO ---
    // Onde tinhas 4 argumentos, agora passas a ter 5
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

        return new AircraftViewDTO(
                aircraft.getRegistrationNumber().toString(),
                aircraft.getAircraftModel().getModelName().toString(),
                aircraft.getManufacturingDate(),
                aircraft.getActualSeatingCapacity(),
                aircraft.getStatus().toString(),
                aircraft.getAircraftModel().getModelPhotoUrl() // <-- Adicionar isto aqui
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
}