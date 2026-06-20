package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.ComponentCategory;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord> findByAircraft_RegistrationNumber(pt.isep.psoft.aisafe.domain.RegistrationNumber registrationNumber);

    List<MaintenanceRecord> findByStatus(pt.isep.psoft.aisafe.domain.MaintenanceRecordStatus status);

    @Query("SELECT m FROM MaintenanceRecord m WHERE " +
            "(:reg IS NULL OR m.aircraft.registrationNumber.number = :reg) AND " +
            "(:startDate IS NULL OR m.startDate >= :startDate) AND " +
            "(:endDate IS NULL OR m.startDate <= :endDate) AND " +
            "(:category IS NULL OR m.componentCategory = :category)")
    Page<MaintenanceRecord> searchRecords(
            @Param("reg") String registrationNumber,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("category") ComponentCategory category,
            Pageable pageable
    );

    // US220 - Soma de custos por avião
    @Query("SELECT m.aircraft.registrationNumber.number, SUM(m.cost) FROM MaintenanceRecord m GROUP BY m.aircraft.registrationNumber.number")
    List<Object[]> getMaintenanceCostsByAircraft();

    // US220 - Soma de custos por MODELO de avião
    @Query("SELECT m.aircraft.model.modelName, SUM(m.cost) FROM MaintenanceRecord m GROUP BY m.aircraft.model.modelName")
    List<Object[]> getMaintenanceCostsByAircraftModel();

    // US222 - Encontra o último registo de manutenção concluído para um avião específico
    Optional<MaintenanceRecord> findTopByAircraftAndStatusOrderByCompletionDateDesc(
            pt.isep.psoft.aisafe.domain.Aircraft aircraft,
            pt.isep.psoft.aisafe.domain.MaintenanceRecordStatus status
    );
}