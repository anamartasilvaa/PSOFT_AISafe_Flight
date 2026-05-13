package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.Airport;
import pt.isep.psoft.aisafe.domain.IATACode;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {

    // Pesquisa por Nome (Contendo parte do texto e ignorando case)
    List<Airport> findByNameContainingIgnoreCase(String name);

    List<Airport> findByCityIgnoreCase(String city);

    List<Airport> findByCountryIgnoreCase(String country);

    @Query("SELECT a FROM Airport a WHERE a.iataCode.code = :iataCode")
    Optional<Airport> findByIataCodeString(@Param("iataCode") String iataCode);
}