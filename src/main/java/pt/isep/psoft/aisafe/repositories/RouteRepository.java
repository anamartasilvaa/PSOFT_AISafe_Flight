package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Route;
import pt.isep.psoft.aisafe.domain.RouteId;

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
}