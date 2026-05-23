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
}