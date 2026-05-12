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

    // Atualizado para incluir os dois repositórios
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

    // --- NOVO MÉTODO PARA A US102 ---
    @Transactional
    public void registerAircraft(RegisterAircraftDTO dto) {
        // 1. Procurar o modelo na base de dados pelo nome
        AircraftModel model = modelRepository.findByModelName(new ModelName(dto.modelName()))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + dto.modelName()));

        // 2. Criar a entidade do Avião (o Domínio valida as regras todas)
        Aircraft aircraft = new Aircraft(
                new RegistrationNumber(dto.registrationNumber()),
                model,
                dto.manufacturingDate(),
                dto.seatingCapacity()
        );

        // 3. Guardar na base de dados
        aircraftRepository.save(aircraft);
    }

    public AircraftViewDTO getAircraftByRegistrationNumber(String regNum) {
        // 1. Vai à base de dados procurar a matrícula
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        // 2. Converte a Entidade para um DTO de visualização
        return new AircraftViewDTO(
                aircraft.getRegistrationNumber().toString(), // Lê a matrícula
                aircraft.getAircraftModel().getModelName().toString(), // Lê o nome do modelo
                aircraft.getManufacturingDate(),
                aircraft.getActualSeatingCapacity(),
                aircraft.getStatus().toString()
        );
    }

    public List<AircraftViewDTO> searchAircrafts(String model, String status) {
        List<Aircraft> results;

        if (status != null) {
            results = aircraftRepository.findByStatus(AircraftStatus.valueOf(status.toUpperCase()));
        } else if (model != null) {
            results = aircraftRepository.findByModel_ModelName(new ModelName(model));
        } else {
            results = aircraftRepository.findAll(); // Se não pesquisar nada, devolve todos
        }

        // Converte a lista de Entidades para uma lista de DTOs
        return results.stream()
                .map(aircraft -> new AircraftViewDTO(
                        aircraft.getRegistrationNumber().toString(),
                        aircraft.getAircraftModel().getModelName().toString(),
                        aircraft.getManufacturingDate(),
                        aircraft.getActualSeatingCapacity(),
                        aircraft.getStatus().toString()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public AircraftViewDTO updateAircraftStatus(String regNum, UpdateAircraftStatusDTO dto) {
        // 1. Procurar o avião na base de dados
        Aircraft aircraft = aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum))
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found: " + regNum));

        // 2. Atualizar o estado (o teu Domínio vai validar se o estado é válido!)
        aircraft.updateStatus(AircraftStatus.valueOf(dto.status().toUpperCase()));

        // 3. Guardar as alterações
        aircraftRepository.save(aircraft);

        // 4. Devolver os dados atualizados para o ecrã
        return new AircraftViewDTO(
                aircraft.getRegistrationNumber().toString(),
                aircraft.getAircraftModel().getModelName().toString(),
                aircraft.getManufacturingDate(),
                aircraft.getActualSeatingCapacity(),
                aircraft.getStatus().toString()
        );
    }
}