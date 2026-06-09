package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Route;
import pt.isep.psoft.aisafe.domain.RouteId;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByRouteId(RouteId routeId);

    // US113 - Procurar rotas por origem (AGORA COM PAGINAÇÃO)
    Page<Route> findByOrigin_IataCode_Code(String originIata, Pageable pageable);

    // US114 - Procurar rotas por destino (AGORA COM PAGINAÇÃO)
    Page<Route> findByDestination_IataCode_Code(String destinationIata, Pageable pageable);

    // US114 - Procurar por Origem e Destino (AGORA COM PAGINAÇÃO)
    Page<Route> findByOrigin_IataCode_CodeAndDestination_IataCode_Code(String originIata, String destinationIata, Pageable pageable);

    // US203 - Procurar rotas compatíveis com uma aeronave (Alcance e Capacidade)
    @Query("SELECT r FROM Route r WHERE r.status = 'ACTIVE' AND r.minimumRange <= :maxRange AND r.minimumCapacity <= :actualCapacity")
    org.springframework.data.domain.Page<Route> findCompatibleRoutes(
            @org.springframework.data.repository.query.Param("maxRange") Double maxRange,
            @org.springframework.data.repository.query.Param("actualCapacity") Integer actualCapacity,
            Pageable pageable);

    // US209
    @Query("SELECT r FROM Route r WHERE r.origin.iataCode.code = :iataCode OR r.destination.iataCode.code = :iataCode")
    Page<Route> findRoutesByAirport(@Param("iataCode") String iataCode, Pageable pageable);

    //US210
    @Query("SELECT COUNT(r) FROM Route r WHERE r.origin.iataCode.code = :iataCode OR r.destination.iataCode.code = :iataCode")
    long countRoutesForAirport(@org.springframework.data.repository.query.Param("iataCode") String iataCode);
}