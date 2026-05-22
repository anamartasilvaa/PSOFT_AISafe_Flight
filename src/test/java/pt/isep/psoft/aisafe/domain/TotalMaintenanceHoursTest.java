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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TotalMaintenanceHoursTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void shouldCalculateTotalHoursOnlyForCompletedRecords() {
        MaintenanceRecord completedRecord = mock(MaintenanceRecord.class);
        when(completedRecord.getStatus()).thenReturn(MaintenanceRecordStatus.COMPLETED);
        when(completedRecord.getExpectedDuration()).thenReturn(10);

        MaintenanceRecord scheduledRecord = mock(MaintenanceRecord.class);
        when(scheduledRecord.getStatus()).thenReturn(MaintenanceRecordStatus.SCHEDULED);

        when(recordRepository.findAll()).thenReturn(List.of(completedRecord, scheduledRecord));

        Integer totalHours = maintenanceService.getTotalMaintenanceHours();

        assertEquals(10, totalHours);
        verify(recordRepository, times(1)).findAll();
    }
}