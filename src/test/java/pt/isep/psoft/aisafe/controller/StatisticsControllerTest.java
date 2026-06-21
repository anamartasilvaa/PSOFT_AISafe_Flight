package pt.isep.psoft.aisafe.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel; // IMPORTANTE: Adiciona este import
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// IMPORTANTE: Corrige o import do Controller para onde ele realmente está
import pt.isep.psoft.aisafe.api.StatisticsController;
import pt.isep.psoft.aisafe.application.StatisticsService;
import pt.isep.psoft.aisafe.application.DTO.AircraftUtilizationRateDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteUtilizationDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsControllerTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsController statisticsController;
    @Test
    void shouldReturn200OkWhenGettingMostFrequentRoutes() {
        RouteUtilizationDTO dto = new RouteUtilizationDTO("RT-OPOLIS", 5L);
        when(statisticsService.getMostFrequentRoutes()).thenReturn(List.of(dto));

        ResponseEntity<CollectionModel<EntityModel<RouteUtilizationDTO>>> response =
                statisticsController.getMostFrequentRoutes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void shouldReturn200OkWhenGettingUtilizationOverTime() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

        AircraftUtilizationRateDTO dto = new AircraftUtilizationRateDTO("CS-TPA", "2026-06", 10L);

        when(statisticsService.getUtilizationRateOverTime(start, end)).thenReturn(List.of(dto));

        ResponseEntity<CollectionModel<EntityModel<AircraftUtilizationRateDTO>>> response =
                statisticsController.getUtilizationOverTime(start, end);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        assertEquals("CS-TPA", response.getBody().getContent().iterator().next().getContent().registrationNumber());
    }
}