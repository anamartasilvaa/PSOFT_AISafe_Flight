package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.DTO.CompleteMaintenanceDTO;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.domain.MaintenanceRecord;
import pt.isep.psoft.aisafe.repositories.MaintenanceRecordRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompleteMaintenanceTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void shouldCompleteMaintenanceSuccessfully() {
        Long recordId = 1L;
        CompleteMaintenanceDTO dto = new CompleteMaintenanceDTO("Trabalho concluído com sucesso.");
        MaintenanceRecord mockRecord = mock(MaintenanceRecord.class);

        when(recordRepository.findById(recordId)).thenReturn(Optional.of(mockRecord));
        when(recordRepository.save(any(MaintenanceRecord.class))).thenReturn(mockRecord);

        MaintenanceRecord result = maintenanceService.completeMaintenance(recordId, dto);

        assertNotNull(result);
        verify(mockRecord, times(1)).complete("Trabalho concluído com sucesso.");
        verify(recordRepository, times(1)).save(mockRecord);
    }
}