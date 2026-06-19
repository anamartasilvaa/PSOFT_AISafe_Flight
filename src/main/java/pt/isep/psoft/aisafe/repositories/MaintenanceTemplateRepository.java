package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.AircraftModel;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, Long> {

    // Se for preciso de procurar templates pelo nome
    Optional<MaintenanceTemplate> findByTemplateName(String templateName);

    List<MaintenanceTemplate> findByAppliesToContaining(AircraftModel model);

}