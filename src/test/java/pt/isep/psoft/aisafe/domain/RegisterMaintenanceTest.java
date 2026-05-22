package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.DTO.RegisterMaintenanceDTO;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.MaintenanceRecordRepository;
import pt.isep.psoft.aisafe.repositories.MaintenanceTemplateRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterMaintenanceTest {

    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private MaintenanceTemplateRepository templateRepository;
    @Mock
    private MaintenanceRecordRepository recordRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void shouldRegisterMaintenanceSuccessfully() {
        RegisterMaintenanceDTO dto = new RegisterMaintenanceDTO(
                "CS-TPA", 1L, "Inspeção", 120, ComponentCategory.ENGINE, "2026-05-22"
        );

        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.of(mock(Aircraft.class)));
        when(templateRepository.findById(1L)).thenReturn(Optional.of(mock(MaintenanceTemplate.class)));

        MaintenanceRecord mockRecord = mock(MaintenanceRecord.class);
        when(recordRepository.save(any(MaintenanceRecord.class))).thenReturn(mockRecord);

        MaintenanceRecord result = maintenanceService.registerMaintenance(dto);

        assertNotNull(result);
        verify(recordRepository, times(1)).save(any(MaintenanceRecord.class));
    }

    @Test
    void shouldThrowExceptionWhenAircraftNotFound() {
        RegisterMaintenanceDTO dto = new RegisterMaintenanceDTO(
                "CS-FAKE", 1L, "Inspeção", 120, ComponentCategory.ENGINE, "2026-05-22"
        );

        when(aircraftRepository.findByRegistrationNumber(any(RegistrationNumber.class))).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            maintenanceService.registerMaintenance(dto);
        });

        assertTrue(exception.getMessage().contains("Aircraft not found"));
        verify(recordRepository, never()).save(any());
    }
}