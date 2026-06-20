package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.DTO.LowStockAlertDTO;
import pt.isep.psoft.aisafe.application.MaintenanceService;
import pt.isep.psoft.aisafe.domain.MaintenancePart;
import pt.isep.psoft.aisafe.domain.PartNumber;
import pt.isep.psoft.aisafe.repositories.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private MaintenanceTemplateRepository templateRepository;
    @Mock
    private MaintenanceRecordRepository recordRepository;
    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private AircraftModelRepository aircraftModelRepository;
    @Mock
    private MaintenancePartRepository partRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    private MaintenancePart criticalPart;

    @BeforeEach
    void setUp() {
        criticalPart = Mockito.mock(MaintenancePart.class);
    }

    @Test
    void givenLowStockParts_whenGenerateLowStockAlerts_thenReturnsAlertList() {
        PartNumber mockPartNumber = Mockito.mock(PartNumber.class);
        when(mockPartNumber.getNumber()).thenReturn("PN-999");

        when(criticalPart.getPartNumber()).thenReturn(mockPartNumber);
        when(criticalPart.getName()).thenReturn("Filtro XYZ");
        when(criticalPart.getStockQuantity()).thenReturn(2);
        when(criticalPart.getMinimumThreshold()).thenReturn(5);

        when(partRepository.findPartsWithLowStock()).thenReturn(List.of(criticalPart));

        List<LowStockAlertDTO> result = maintenanceService.generateLowStockAlerts();


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PN-999", result.get(0).partNumber());
        assertTrue(result.get(0).alertMessage().contains("CRITICAL"));
    }

    @Test
    void givenHealthyInventory_whenGenerateLowStockAlerts_thenReturnsEmptyList() {
        when(partRepository.findPartsWithLowStock()).thenReturn(Collections.emptyList());

        List<LowStockAlertDTO> result = maintenanceService.generateLowStockAlerts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}