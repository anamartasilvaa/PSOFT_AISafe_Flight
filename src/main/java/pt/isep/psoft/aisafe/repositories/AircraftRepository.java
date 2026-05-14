package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.AircraftStatus;
import pt.isep.psoft.aisafe.domain.ModelName;
import pt.isep.psoft.aisafe.domain.RegistrationNumber;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Optional<Aircraft> findByRegistrationNumber(RegistrationNumber registrationNumber);

    // Trocar Page por List e remover Pageable
    List<Aircraft> findByStatus(AircraftStatus status);
    List<Aircraft> findByModel_ModelName(ModelName modelName);

    @Query("SELECT a FROM Aircraft a WHERE YEAR(a.manufacturingDate) = :year")
    List<Aircraft> findByManufacturingYear(@Param("year") int year);
}