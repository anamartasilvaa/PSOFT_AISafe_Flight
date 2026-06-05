package pt.isep.psoft.aisafe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.isep.psoft.aisafe.api.AircraftController;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.AircraftViewDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAircraftDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAircraftStatusDTO;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AircraftControllerTest {

    @Mock
    private AircraftService aircraftService;

    @InjectMocks
    private AircraftController aircraftController;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldReturn201CreatedWhenCreatingAircraft() {

        RegisterAircraftDTO requestDto = new RegisterAircraftDTO("CS-TPA", "A320", LocalDate.of(2023, 5, 10), 180);

        ResponseEntity<EntityModel<RegisterAircraftDTO>> response = aircraftController.createAircraft(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CS-TPA", response.getBody().getContent().registrationNumber());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }

    @Test
    void shouldReturn200OkWhenUpdatingAircraftStatus() {
        String regNum = "CS-TPA";
        UpdateAircraftStatusDTO requestDto = new UpdateAircraftStatusDTO("UNDER_MAINTENANCE");
        AircraftViewDTO responseDto = new AircraftViewDTO(regNum, "A320", LocalDate.of(2023, 5, 10), 180, "UNDER_MAINTENANCE", "url");

        when(aircraftService.updateAircraftStatus(eq(regNum), any(UpdateAircraftStatusDTO.class))).thenReturn(responseDto);

        ResponseEntity<EntityModel<AircraftViewDTO>> response = aircraftController.updateStatus(regNum, requestDto);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UNDER_MAINTENANCE", response.getBody().getContent().status());
    }

    @Test
    void shouldReturn200OkWhenGettingAircraft() {

        String regNum = "CS-TPA";
        AircraftViewDTO responseDto = new AircraftViewDTO(regNum, "A320", LocalDate.of(2023, 5, 10), 180, "ACTIVE", "url");

        when(aircraftService.getAircraftByRegistrationNumber(eq(regNum))).thenReturn(responseDto);

        ResponseEntity<EntityModel<AircraftViewDTO>> response = aircraftController.getAircraft(regNum);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(regNum, response.getBody().getContent().registrationNumber());
    }

    // ==========================================
    // TESTES DO WP#1B (FASE 2)
    // ==========================================

    @Test
    void shouldReturn200OkWhenUpdatingModelSpecifications() { // US201
        String modelName = "B737 MAX";
        pt.isep.psoft.aisafe.application.DTO.UpdateAircraftModelSpecsDTO requestDto =
                new pt.isep.psoft.aisafe.application.DTO.UpdateAircraftModelSpecsDTO(200, 20000.0, 5000.0, 800.0, "3-3", null);

        pt.isep.psoft.aisafe.application.DTO.AircraftModelViewDTO responseDto =
                new pt.isep.psoft.aisafe.application.DTO.AircraftModelViewDTO(modelName, "BOEING", 200, 20000.0, 5000.0, 800.0, "url", "3-3", null);

        when(aircraftService.updateModelSpecifications(eq(modelName), any())).thenReturn(responseDto);

        ResponseEntity<EntityModel<pt.isep.psoft.aisafe.application.DTO.AircraftModelViewDTO>> response =
                aircraftController.updateModelSpecifications(modelName, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(modelName, response.getBody().getContent().modelName());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }

    @Test
    void shouldReturn200OkWhenGettingTop5Models() { // US204
        pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO top1 = new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO("A320neo", 15000.0);
        pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO top2 = new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO("B737 MAX", 10000.0);

        when(aircraftService.getTop5UtilizedModels()).thenReturn(java.util.List.of(top1, top2));

        ResponseEntity<org.springframework.hateoas.CollectionModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO>>> response =
                aircraftController.getTop5Models();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertTrue(response.getBody().hasLinks(), "Collection should contain a self link");
    }

    @Test
    void shouldReturn200OkWhenGettingCompatibleRoutes() { // US203
        String regNum = "CS-TPA";
        pt.isep.psoft.aisafe.application.DTO.RouteViewDTO route1 = new pt.isep.psoft.aisafe.application.DTO.RouteViewDTO("RT-OPOLIS", "OPO", "LIS", "ACTIVE", 100);

        when(aircraftService.getCompatibleRoutesForAircraft(regNum)).thenReturn(java.util.List.of(route1));

        ResponseEntity<org.springframework.hateoas.CollectionModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO>>> response =
                aircraftController.getCompatibleRoutes(regNum);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
        assertTrue(response.getBody().hasLinks(), "Collection should contain a self link");
    }
}