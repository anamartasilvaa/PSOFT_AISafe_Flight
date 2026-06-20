package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.AircraftStatus;
import pt.isep.psoft.aisafe.domain.ModelName;
import pt.isep.psoft.aisafe.domain.RegistrationNumber;

import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {
    Optional<Aircraft> findByRegistrationNumber(RegistrationNumber registrationNumber);

    List<Aircraft> findByStatus(AircraftStatus status);
    List<Aircraft> findByModel_ModelName(ModelName modelName);

    @Query("SELECT a FROM Aircraft a WHERE YEAR(a.manufacturingDate) = :year")
    List<Aircraft> findByManufacturingYear(@Param("year") int year);

    @Query("SELECT new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO(a.model.modelName.name, SUM(a.totalFlightHours)) " +
            "FROM Aircraft a GROUP BY a.model.modelName.name ORDER BY SUM(a.totalFlightHours) DESC")
    List<TopAircraftModelDTO> findTop5ModelsByFlightHours(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO(a.model.modelName.name, COUNT(sf)) " +
            "FROM ScheduledFlight sf JOIN sf.aircraft a GROUP BY a.model.modelName.name ORDER BY COUNT(sf) DESC")
    List<TopAircraftModelDTO> findTop5ModelsByAssignments(org.springframework.data.domain.Pageable pageable);

    // --- US224 ---
    @Query("SELECT a FROM Aircraft a WHERE " +
            "(:engineType IS NULL OR a.aircraftModel.engineType = :engineType) AND " +
            "(:feature IS NULL OR a.features LIKE %:feature%)")
    List<Aircraft> findByFeaturesAndEngine(@Param("feature") String feature,
                                           @Param("engineType") String engineType);
}