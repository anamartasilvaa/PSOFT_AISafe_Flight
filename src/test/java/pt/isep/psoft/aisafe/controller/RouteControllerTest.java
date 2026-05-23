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

import pt.isep.psoft.aisafe.api.RouteController;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.RouteService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    @Mock
    private RouteService routeService;

    @InjectMocks
    private RouteController routeController;

    @BeforeEach
    void setUp() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    // --- TESTE 1: Simular a criação de uma rota ---
    @Test
    void shouldReturn201CreatedWhenCreatingRoute() {
        CreateRouteDTO requestDto = new CreateRouteDTO("RT-OPOLIS", "OPO", "LIS", 45, 300.0, 150);
        RouteViewDTO responseDto = new RouteViewDTO("RT-OPOLIS", "OPO", "LIS", "ACTIVE");

        when(routeService.createRoute(any(CreateRouteDTO.class))).thenReturn(responseDto);


        ResponseEntity<EntityModel<RouteViewDTO>> response = routeController.createRoute(requestDto);


        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RT-OPOLIS", response.getBody().getContent().routeId());
        assertTrue(response.getBody().hasLinks());
    }

    // --- TESTE 2: Simular a desativação de uma rota ---
    @Test
    void shouldReturn200OkWhenDeactivatingRoute() {
        String routeId = "RT-OPOLIS";
        RouteViewDTO responseDto = new RouteViewDTO(routeId, "OPO", "LIS", "INACTIVE");

        when(routeService.deactivateRoute(eq(routeId))).thenReturn(responseDto);

        ResponseEntity<EntityModel<RouteViewDTO>> response = routeController.deactivateRoute(routeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INACTIVE", response.getBody().getContent().status());
    }

    // --- TESTE 3: Simular a pesquisa de uma rota por ID  ---
    @Test
    void shouldReturn200OkWhenGettingRouteById() {
        String routeId = "RT-OPOLIS";
        RouteViewDTO responseDto = new RouteViewDTO(routeId, "OPO", "LIS", "ACTIVE");

        when(routeService.getRoute(eq(routeId))).thenReturn(responseDto);

        ResponseEntity<EntityModel<RouteViewDTO>> response = routeController.getRouteById(routeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(routeId, response.getBody().getContent().routeId());
    }
}