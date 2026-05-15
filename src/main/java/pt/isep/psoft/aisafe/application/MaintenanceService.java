package pt.isep.psoft.aisafe.application;

import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.CreateMaintenanceTemplateDTO;
import pt.isep.psoft.aisafe.domain.MaintenanceTemplate;
import pt.isep.psoft.aisafe.repositories.MaintenanceTemplateRepository;

@Service
public class MaintenanceService {

    private final MaintenanceTemplateRepository templateRepository;

    public MaintenanceService(MaintenanceTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    // US115 - Lógica para criar o manual de manutenção
    public MaintenanceTemplate createTemplate(CreateMaintenanceTemplateDTO dto) {

        if (templateRepository.findByName(dto.name()).isPresent()) {
            throw new IllegalArgumentException("Já existe um template com o nome: " + dto.name());
        }

        MaintenanceTemplate template = new MaintenanceTemplate(dto.name(), dto.type(), dto.checklist());
        return templateRepository.save(template);
    }
}