package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Route;
import pt.isep.psoft.aisafe.domain.RouteId;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, RouteId> {

    // US113 - Procurar rotas por origem
    List<Route> findByOrigin_IataCode_Code(String originIata);

    // US114 - Procurar rotas por destino
    List<Route> findByDestination_IataCode_Code(String destinationIata);

    // US114 - Procurar por Origem e Destino
    List<Route> findByOrigin_IataCode_CodeAndDestination_IataCode_Code(String originIata, String destinationIata);
}