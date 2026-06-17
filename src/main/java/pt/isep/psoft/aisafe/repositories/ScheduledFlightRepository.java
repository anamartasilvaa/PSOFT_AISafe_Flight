package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.FlightStatus;
import pt.isep.psoft.aisafe.domain.ScheduledFlight;
import pt.isep.psoft.aisafe.domain.RegistrationNumber; // Não te esqueças deste import!
import java.util.List; // E deste!

@Repository
public interface ScheduledFlightRepository extends JpaRepository<ScheduledFlight, Long> {

    boolean existsByAircraftAndStatus(Aircraft aircraft, FlightStatus status);

    List<ScheduledFlight> findByAircraft_RegistrationNumber(RegistrationNumber registrationNumber);
}