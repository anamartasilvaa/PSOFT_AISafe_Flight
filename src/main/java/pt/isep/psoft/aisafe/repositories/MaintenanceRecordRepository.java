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

    // US220 - Soma de custos por avião (Retorna pares de [String, Double])
    @Query("SELECT m.aircraft.registrationNumber.number, SUM(m.cost) FROM MaintenanceRecord m GROUP BY m.aircraft")
    List<Object[]> getMaintenanceCostsByAircraft();
}