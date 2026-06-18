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
import pt.isep.psoft.aisafe.api.FlightController;
import pt.isep.psoft.aisafe.application.FlightService;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.application.DTO.ScheduledFlightViewDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private FlightController flightController;

    @BeforeEach
    void setUp() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldReturn201CreatedWhenSchedulingFlight() { // US212
        CreateScheduledFlightDTO requestDto = new CreateScheduledFlightDTO("RT-OPOLIS", "CS-TPA", "2026-10-10T10:00:00");
        ScheduledFlightViewDTO responseDto = new ScheduledFlightViewDTO(1L, "RT-OPOLIS", "CS-TPA", "2026-10-10T10:00:00", "SCHEDULED");

        when(flightService.scheduleFlight(any(CreateScheduledFlightDTO.class))).thenReturn(responseDto);

        ResponseEntity<EntityModel<ScheduledFlightViewDTO>> response = flightController.scheduleFlight(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RT-OPOLIS", response.getBody().getContent().routeId());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }

    @Test
    void shouldReturn200OkWhenGettingScheduledFlights() { // US213
        String regNum = "CS-TPA";
        ScheduledFlightViewDTO flight1 = new ScheduledFlightViewDTO(1L, "RT-OPOLIS", regNum, "2026-10-10T10:00:00", "SCHEDULED");


        when(flightService.getScheduledFlightsByAircraft(eq(regNum))).thenReturn(List.of(flight1));

        ResponseEntity<org.springframework.hateoas.CollectionModel<EntityModel<ScheduledFlightViewDTO>>> response =
                flightController.getFlightsByAircraft(regNum);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }
}