package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Aircraft;
import pt.isep.psoft.aisafe.domain.FlightStatus;
import pt.isep.psoft.aisafe.domain.ScheduledFlight;
import pt.isep.psoft.aisafe.domain.RegistrationNumber;
import java.util.List;

@Repository
public interface ScheduledFlightRepository extends JpaRepository<ScheduledFlight, Long> {

    boolean existsByAircraftAndStatus(Aircraft aircraft, FlightStatus status);

    List<ScheduledFlight> findByAircraft_RegistrationNumber(RegistrationNumber registrationNumber);

    //  US229
    @Query("SELECT f.route.routeId.id, COUNT(f) " +
            "FROM ScheduledFlight f " +
            "GROUP BY f.route.routeId.id " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countFlightsGroupedByRoute();
}