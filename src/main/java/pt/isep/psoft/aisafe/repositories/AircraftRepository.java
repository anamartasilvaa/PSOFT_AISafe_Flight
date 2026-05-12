package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.AircraftStatus;
import pt.isep.psoft.aisafe.domain.ModelName;
import pt.isep.psoft.aisafe.domain.RegistrationNumber;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    // O Spring faz a query SQL automaticamente só por leres este nome!
    Optional<Aircraft> findByRegistrationNumber(RegistrationNumber registrationNumber);

    // Pesquisa por Estado
    List<Aircraft> findByStatus(AircraftStatus status);

    // Pesquisa por Modelo
    List<Aircraft> findByModel_ModelName(ModelName modelName);
}