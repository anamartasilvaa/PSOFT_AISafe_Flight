package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Airport;
import pt.isep.psoft.aisafe.domain.IATACode;
import java.util.Optional;

@Repository
public interface AirportRepository extends CrudRepository<Airport, Long> {

    /**
     * Procura um aeroporto pela sua identidade de domínio (IATA Code).
     * O Spring gera automaticamente: SELECT * FROM airport WHERE iata_code = ?
     */
    Optional<Airport> findByIataCode(IATACode iataCode);
}