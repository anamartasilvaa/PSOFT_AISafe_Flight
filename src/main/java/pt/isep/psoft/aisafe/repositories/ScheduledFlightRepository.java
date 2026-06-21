package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.*;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ScheduledFlightRepository extends JpaRepository<ScheduledFlight, Long> {

    boolean existsByAircraftAndStatus(Aircraft aircraft, FlightStatus status);
    List<ScheduledFlight> findByAircraft_RegistrationNumber(RegistrationNumber registrationNumber);

    // US227: Eficiência de combustível por avião
    @Query("SELECT f.aircraft.registrationNumber.number, SUM(f.route.estimatedFlightTime * f.aircraft.model.fuelConsumptionPerHour) " +
            "FROM ScheduledFlight f GROUP BY f.aircraft.registrationNumber.number")
    List<Object[]> getFuelEfficiencyPerAircraft();

    // US227: Eficiência de combustível por rota
    @Query("SELECT f.route.routeId.id, AVG(f.route.estimatedFlightTime * f.aircraft.model.fuelConsumptionPerHour) " +
            "FROM ScheduledFlight f GROUP BY f.route.routeId.id")
    List<Object[]> getFuelEfficiencyPerRoute();

    // US229: Rotas mais frequentes (COM ORDENAÇÃO)
    @Query("SELECT f.route.routeId.id, COUNT(f) " +
            "FROM ScheduledFlight f " +
            "GROUP BY f.route.routeId.id " +
            "ORDER BY COUNT(f) DESC")
    List<Object[]> countFlightsGroupedByRoute();

    // US223: Utilização por avião e por mês (COM AGRUPAMENTO)
    @Query("SELECT f.aircraft.registrationNumber.number, FUNCTION('DATE_FORMAT', f.scheduledDateTime, '%Y-%m'), COUNT(f) " +
            "FROM ScheduledFlight f " +
            "WHERE f.scheduledDateTime BETWEEN :start AND :end " +
            "GROUP BY f.aircraft.registrationNumber.number, FUNCTION('DATE_FORMAT', f.scheduledDateTime, '%Y-%m')")
    List<Object[]> countFlightsByAircraftAndMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}