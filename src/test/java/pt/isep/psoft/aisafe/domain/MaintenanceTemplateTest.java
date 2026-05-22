package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.DTO.CreateMaintenanceTemplateDTO;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import pt.isep.psoft.aisafe.repositories.MaintenanceTemplateRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceTemplateTest {

    @Mock
    private MaintenanceTemplateRepository templateRepository;
    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void shouldCreateTemplateSuccessfully() {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO(
                "Check A", "PREVENTIVE", 125.0, 90, "Checklist A", "v1.0", new ArrayList<>(), new ArrayList<>()
        );

        when(templateRepository.findByTemplateName("Check A")).thenReturn(Optional.empty());
        MaintenanceTemplate mockTemplate = mock(MaintenanceTemplate.class);
        when(templateRepository.save(any(MaintenanceTemplate.class))).thenReturn(mockTemplate);

        MaintenanceTemplate result = maintenanceService.createTemplate(dto);

        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(MaintenanceTemplate.class));
    }

    @Test
    void shouldThrowExceptionWhenTemplateNameAlreadyExists() {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO(
                "Check A", "PREVENTIVE", 125.0, 90, "Checklist A", "v1.0", new ArrayList<>(), new ArrayList<>()
        );

        when(templateRepository.findByTemplateName("Check A")).thenReturn(Optional.of(mock(MaintenanceTemplate.class)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            maintenanceService.createTemplate(dto);
        });

        assertTrue(exception.getMessage().contains("Template name already exists"));
        verify(templateRepository, never()).save(any());
    }
}