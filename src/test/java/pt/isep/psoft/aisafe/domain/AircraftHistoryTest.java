package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.repositories.MaintenanceRecordRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftHistoryTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void shouldReturnAircraftMaintenanceHistory() {
        String registration = "CS-TPA";
        List<MaintenanceRecord> mockList = List.of(mock(MaintenanceRecord.class), mock(MaintenanceRecord.class));

        when(recordRepository.findByAircraft_RegistrationNumber(any(RegistrationNumber.class))).thenReturn(mockList);

        List<MaintenanceRecord> result = maintenanceService.getAircraftHistory(registration);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(recordRepository, times(1)).findByAircraft_RegistrationNumber(any(RegistrationNumber.class));
    }
}