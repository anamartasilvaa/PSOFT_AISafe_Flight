package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.AircraftStatus;
import pt.isep.psoft.aisafe.domain.ModelName;
import pt.isep.psoft.aisafe.domain.RegistrationNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    Optional<Aircraft> findByRegistrationNumber(RegistrationNumber registrationNumber);

    // O Spring Boot vai usar o Pageable para meter os LIMITS no SQL automaticamente!
    Page<Aircraft> findByStatus(AircraftStatus status, Pageable pageable);

    Page<Aircraft> findByModel_ModelName(ModelName modelName, Pageable pageable);
}