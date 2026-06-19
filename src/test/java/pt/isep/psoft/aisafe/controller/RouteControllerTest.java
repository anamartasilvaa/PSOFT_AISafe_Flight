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
        // ADICIONADO O 150 NO FINAL:
        RouteViewDTO responseDto = new RouteViewDTO("RT-OPOLIS", "OPO", "LIS", "ACTIVE", 150);

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
        // ADICIONADO O 150 NO FINAL:
        RouteViewDTO responseDto = new RouteViewDTO(routeId, "OPO", "LIS", "INACTIVE", 150);

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

        RouteViewDTO responseDto = new RouteViewDTO(routeId, "OPO", "LIS", "ACTIVE", 150);

        when(routeService.getRoute(eq(routeId))).thenReturn(responseDto);

        ResponseEntity<EntityModel<RouteViewDTO>> response = routeController.getRouteById(routeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(routeId, response.getBody().getContent().routeId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturn200OkWhenGettingRoutesInvolvingAirport() { // US209
        String iataCode = "OPO";
        pt.isep.psoft.aisafe.application.DTO.RouteViewDTO routeDTO =
                new pt.isep.psoft.aisafe.application.DTO.RouteViewDTO("RT-OPOLIS", "OPO", "LIS", "ACTIVE", 150);

        org.springframework.data.domain.Page<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO> mockPage =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(routeDTO));

        when(routeService.getRoutesByAirport(eq(iataCode), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        org.springframework.data.web.PagedResourcesAssembler assemblerMock =
                org.mockito.Mockito.mock(org.springframework.data.web.PagedResourcesAssembler.class);

        org.springframework.hateoas.PagedModel pagedModel = org.springframework.hateoas.PagedModel.of(
                java.util.List.of(EntityModel.of(routeDTO)),
                new org.springframework.hateoas.PagedModel.PageMetadata(1, 0, 1)
        );

        when(assemblerMock.toModel(
                any(org.springframework.data.domain.Page.class),
                any(org.springframework.hateoas.server.RepresentationModelAssembler.class)
        )).thenReturn(pagedModel);

        ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO>>> response =
                (ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO>>>) (ResponseEntity<?>)
                        routeController.getRoutesInvolvingAirport(iataCode, org.springframework.data.domain.PageRequest.of(0, 10), assemblerMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void shouldReturn200OkWhenGettingBusiestAirports() { // US210
        pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO stats1 =
                new pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO("LHR", "Heathrow", 500);
        pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO stats2 =
                new pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO("JFK", "John F Kennedy", 450);

        when(routeService.getBusiestAirports()).thenReturn(java.util.List.of(stats1, stats2));


        ResponseEntity<org.springframework.hateoas.CollectionModel<org.springframework.hateoas.EntityModel<pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO>>> response =
                routeController.getBusiestAirports();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        java.util.Collection<org.springframework.hateoas.EntityModel<pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO>> content = response.getBody().getContent();
        assertEquals(2, content.size());

        pt.isep.psoft.aisafe.application.DTO.BusiestAirportDTO firstItem = content.iterator().next().getContent();
        assertEquals("LHR", firstItem.iataCode());

        assertTrue(response.getBody().hasLinks(), "A resposta deve conter links HATEOAS");
    }

    //US228
    @Test
    void shouldReturn200AndFileWhenExportingGeoJson() {
        // Arrange
        String fakeGeoJson = "{\"type\": \"FeatureCollection\", \"features\": []}";

        // Agora o mock sabe que o serviço real devolve uma String!
        when(routeService.exportRoutesAsGeoJson()).thenReturn(fakeGeoJson);

        // Act
        ResponseEntity<byte[]> response = routeController.exportGeoJson();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(fakeGeoJson.getBytes().length, response.getBody().length);

        // Verifica se o Header Content-Disposition existe sem usar containsKey
        assertNotNull(response.getHeaders().get(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION),
                "Falta o header Content-Disposition para forçar o download");
    }
}