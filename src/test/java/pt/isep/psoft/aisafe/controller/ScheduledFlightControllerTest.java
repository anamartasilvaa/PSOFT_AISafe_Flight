package pt.isep.psoft.aisafe.controller;

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

import pt.isep.psoft.aisafe.api.ScheduledFlightController;
import pt.isep.psoft.aisafe.application.ScheduledFlightService;
import pt.isep.psoft.aisafe.application.DTO.CreateScheduledFlightDTO;
import pt.isep.psoft.aisafe.application.DTO.ScheduledFlightViewDTO;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledFlightControllerTest {

    @Mock
    private ScheduledFlightService scheduledFlightService;

    @InjectMocks
    private ScheduledFlightController scheduledFlightController;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldReturn201CreatedWhenSchedulingFlight() {
        CreateScheduledFlightDTO requestDto = new CreateScheduledFlightDTO("RT-OPOLIS", "CS-TPA", "2026-10-10T10:00:00");
        ScheduledFlightViewDTO responseDto = new ScheduledFlightViewDTO(1L, "RT-OPOLIS", "CS-TPA", "2026-10-10T10:00:00", "SCHEDULED");

        when(scheduledFlightService.scheduleFlight(any(CreateScheduledFlightDTO.class))).thenReturn(responseDto);

        ResponseEntity<EntityModel<ScheduledFlightViewDTO>> response = scheduledFlightController.scheduleFlight(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RT-OPOLIS", response.getBody().getContent().routeId());
    }

    @Test
    void shouldReturn200OkWhenGettingScheduledFlights() {
        String regNum = "CS-TPA";
        ScheduledFlightViewDTO flight1 = new ScheduledFlightViewDTO(1L, "RT-OPOLIS", regNum, "2026-10-10T10:00:00", "SCHEDULED");

        when(scheduledFlightService.getScheduledFlightsByAircraft(eq(regNum))).thenReturn(List.of(flight1));

        ResponseEntity<CollectionModel<EntityModel<ScheduledFlightViewDTO>>> response =
                scheduledFlightController.getFlightsByAircraft(regNum);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void shouldReturn204NoContentWhenNoFlightsFound() {
        String regNum = "CS-INVALIDO";
        when(scheduledFlightService.getScheduledFlightsByAircraft(eq(regNum))).thenReturn(Collections.emptyList());

        ResponseEntity<CollectionModel<EntityModel<ScheduledFlightViewDTO>>> response =
                scheduledFlightController.getFlightsByAircraft(regNum);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void shouldThrowExceptionWhenSchedulingFlightFails() {
        CreateScheduledFlightDTO requestDto = new CreateScheduledFlightDTO("RT-INVALID", "CS-TPA", "2026-10-10T10:00:00");

        when(scheduledFlightService.scheduleFlight(any(CreateScheduledFlightDTO.class)))
                .thenThrow(new IllegalArgumentException("Route not found"));

        assertThrows(IllegalArgumentException.class, () -> {
            scheduledFlightController.scheduleFlight(requestDto);
        });
    }
}