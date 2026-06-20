package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.*;
import java.util.List;

@Repository
public interface ScheduledFlightRepository extends JpaRepository<ScheduledFlight, Long> {

    boolean existsByAircraftAndStatus(Aircraft aircraft, FlightStatus status);
    List<ScheduledFlight> findByAircraft_RegistrationNumber(RegistrationNumber registrationNumber);

    @Query("SELECT f.aircraft.registrationNumber.number, SUM(f.route.estimatedFlightTime * f.aircraft.model.fuelConsumptionPerHour) " +
            "FROM ScheduledFlight f GROUP BY f.aircraft.registrationNumber.number")
    List<Object[]> getFuelEfficiencyPerAircraft();

    @Query("SELECT f.route.routeId.id, AVG(f.route.estimatedFlightTime * f.aircraft.model.fuelConsumptionPerHour) " +
            "FROM ScheduledFlight f GROUP BY f.route.routeId.id")
    List<Object[]> getFuelEfficiencyPerRoute();

    @Query("SELECT f.route.routeId.id, COUNT(f) FROM ScheduledFlight f GROUP BY f.route.routeId.id")
    List<Object[]> countFlightsGroupedByRoute();

    @Query("SELECT FUNCTION('DATE_FORMAT', f.scheduledDateTime, '%Y-%m'), COUNT(f) " +
            "FROM ScheduledFlight f GROUP BY FUNCTION('DATE_FORMAT', f.scheduledDateTime, '%Y-%m')")
    List<Object[]> countFlightsByMonth(java.time.LocalDateTime start, java.time.LocalDateTime end);
}