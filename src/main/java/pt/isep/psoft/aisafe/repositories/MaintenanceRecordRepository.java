package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;

import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    // US116: Find maintenance history for a specific aircraft using its tail number
    List<MaintenanceRecord> findByAircraft_RegistrationNumber(pt.isep.psoft.aisafe.domain.RegistrationNumber registrationNumber);
}