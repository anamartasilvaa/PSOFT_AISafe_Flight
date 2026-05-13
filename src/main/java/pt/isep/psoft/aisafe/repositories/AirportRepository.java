package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.isep.psoft.aisafe.domain.Airport;
import pt.isep.psoft.aisafe.domain.IATACode;
import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    // Esta query garante que o Hibernate foca no campo 'code' dentro do Value Object
    @Query("SELECT a FROM Airport a WHERE a.iataCode.code = :code")
    Optional<Airport> findByIataCodeString(@Param("code") String code);
}