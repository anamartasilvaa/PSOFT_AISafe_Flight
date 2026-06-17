package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Route;
import pt.isep.psoft.aisafe.domain.RouteId;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByRouteId(RouteId routeId);

    Page<Route> findByOrigin_IataCode_Code(String originIata, Pageable pageable);

    Page<Route> findByDestination_IataCode_Code(String destinationIata, Pageable pageable);

    Page<Route> findByOrigin_IataCode_CodeAndDestination_IataCode_Code(String originIata, String destinationIata, Pageable pageable);

    @Query("SELECT r FROM Route r WHERE r.status = 'ACTIVE' AND r.minimumRange <= :maxRange AND r.minimumCapacity <= :actualCapacity")
    Page<Route> findCompatibleRoutes(
            @Param("maxRange") Double maxRange,
            @Param("actualCapacity") Integer actualCapacity,
            Pageable pageable);

    @Query("SELECT r FROM Route r WHERE r.origin.iataCode.code = :iataCode OR r.destination.iataCode.code = :iataCode")
    Page<Route> findRoutesByAirport(@Param("iataCode") String iataCode, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Route r WHERE r.origin.iataCode.code = :iataCode OR r.destination.iataCode.code = :iataCode")
    long countRoutesForAirport(@Param("iataCode") String iataCode);

    // US215 - Distância total da rede
    @Query("SELECT SUM(r.minimumRange) FROM Route r WHERE r.status = 'ACTIVE'")
    Double calculateTotalNetworkDistance();

    // US214 - Rotas ativas ordenadas por distância
    @Query("SELECT r FROM Route r WHERE r.status = 'ACTIVE' ORDER BY r.minimumRange ASC")
    Page<Route> findAllActiveRoutesSortedByDistance(Pageable pageable);

    // US214 - Rotas ativas ordenadas por popularidade (mais voos agendados)
    @Query("SELECT r FROM Route r WHERE r.status = 'ACTIVE' ORDER BY (SELECT COUNT(f) FROM ScheduledFlight f WHERE f.route = r) DESC")
    Page<Route> findAllActiveRoutesSortedByPopularity(Pageable pageable);
}