package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.aisafe.domain.Route;
import pt.isep.psoft.aisafe.domain.RouteId;

public interface RouteRepository extends JpaRepository<Route, RouteId> {

}