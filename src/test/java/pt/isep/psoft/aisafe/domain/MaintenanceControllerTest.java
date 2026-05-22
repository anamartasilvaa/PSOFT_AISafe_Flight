package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import pt.isep.psoft.aisafe.api.MaintenanceController;
import pt.isep.psoft.aisafe.application.DTO.CompleteMaintenanceDTO;
import pt.isep.psoft.aisafe.application.DTO.CreateMaintenanceTemplateDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterMaintenanceDTO;
import pt.isep.psoft.aisafe.application.MaintenanceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {

    @Mock
    private MaintenanceService maintenanceService;

    @InjectMocks
    private MaintenanceController maintenanceController;

    @BeforeEach
    void setUp() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    // --- TESTE 1: US115a - Create Template ---
    @Test
    void shouldReturn201WhenCreatingTemplate() {
        CreateMaintenanceTemplateDTO mockDto = mock(CreateMaintenanceTemplateDTO.class);
        MaintenanceTemplate mockTemplate = mock(MaintenanceTemplate.class);

        when(maintenanceService.createTemplate(any())).thenReturn(mockTemplate);

        ResponseEntity<EntityModel<MaintenanceTemplate>> response = maintenanceController.createTemplate(mockDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().hasLinks());
    }

    // --- TESTE 2: US115 - Register Active Maintenance Record ---
    @Test
    void shouldReturn201WhenRegisteringMaintenance() {
        RegisterMaintenanceDTO mockDto = mock(RegisterMaintenanceDTO.class);
        when(mockDto.registrationNumber()).thenReturn("CS-TPA");

        MaintenanceRecord mockRecord = mock(MaintenanceRecord.class);
        when(mockRecord.getPk()).thenReturn(1L);

        when(maintenanceService.registerMaintenance(any())).thenReturn(mockRecord);

        ResponseEntity<EntityModel<MaintenanceRecord>> response = maintenanceController.registerMaintenance(mockDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().hasLinks());
    }

    // --- TESTE 3: US116 - Get History by Aircraft ---
    @Test
    void shouldReturn200WhenGettingAircraftHistory() {
        String registration = "CS-TPA";
        MaintenanceRecord mockRecord = mock(MaintenanceRecord.class);
        when(mockRecord.getStatus()).thenReturn(MaintenanceRecordStatus.SCHEDULED);
        when(mockRecord.getPk()).thenReturn(1L);

        when(maintenanceService.getAircraftHistory(registration)).thenReturn(List.of(mockRecord));

        ResponseEntity<CollectionModel<EntityModel<MaintenanceRecord>>> response = maintenanceController.getAircraftHistory(registration);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().hasLinks());
    }

    // --- TESTE 4: US117 - Get Fleet Total Maintenance Hours ---
    @Test
    void shouldReturn200WhenGettingTotalHours() {
        when(maintenanceService.getTotalMaintenanceHours()).thenReturn(450);

        ResponseEntity<EntityModel<Integer>> response = maintenanceController.getTotalHours();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(450, response.getBody().getContent());
    }

    // --- TESTE 5: US119 - Complete Maintenance ---
    @Test
    void shouldReturn200WhenCompletingMaintenance() {
        Long recordId = 1L;
        CompleteMaintenanceDTO mockDto = mock(CompleteMaintenanceDTO.class);
        MaintenanceRecord mockRecord = mock(MaintenanceRecord.class);

        when(maintenanceService.completeMaintenance(eq(recordId), any())).thenReturn(mockRecord);

        ResponseEntity<EntityModel<MaintenanceRecord>> response = maintenanceController.completeMaintenance(recordId, mockDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().hasLinks());
    }
}
