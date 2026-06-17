package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;
import pt.isep.psoft.aisafe.domain.AircraftModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, Long> {

    Optional<MaintenanceTemplate> findByTemplateName(String templateName);

    // Encontra todos os templates que se aplicam a um determinado modelo de avião
    List<MaintenanceTemplate> findByModelsContaining(AircraftModel model);
}