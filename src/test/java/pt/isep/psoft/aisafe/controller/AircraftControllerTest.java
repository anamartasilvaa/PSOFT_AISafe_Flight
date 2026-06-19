package pt.isep.psoft.aisafe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.isep.psoft.aisafe.api.AircraftController;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturn200OkWhenUpdatingAircraftStatus() {
        String regNum = "CS-TPA";
        UpdateAircraftStatusDTO requestDto = new UpdateAircraftStatusDTO("UNDER_MAINTENANCE");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("aircraft", new AircraftViewDTO(regNum, "A320", LocalDate.of(2023, 5, 10), 180, "UNDER_MAINTENANCE", "url"));
        mockResponse.put("swapReport", List.of("Algorithm executed"));

        when(aircraftService.updateAircraftStatusWithReport(eq(regNum), any())).thenReturn(mockResponse);

        ResponseEntity<EntityModel<Map<String, Object>>> response = aircraftController.updateStatus(regNum, requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getContent().get("swapReport"));
    }

    @Test
    void shouldReturn200OkWhenGettingAircraft() {
        String regNum = "CS-TPA";
        AircraftViewDTO responseDto = new AircraftViewDTO(regNum, "A320", LocalDate.of(2023, 5, 10), 180, "ACTIVE", "url");
        when(aircraftService.getAircraftByRegistrationNumber(eq(regNum))).thenReturn(responseDto);

        ResponseEntity<EntityModel<AircraftViewDTO>> response = aircraftController.getAircraft(regNum);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldReturn200OkWhenUpdatingModelSpecifications() {
        String modelName = "B737 MAX";
        UpdateAircraftModelSpecsDTO requestDto = new UpdateAircraftModelSpecsDTO(200, 20000.0, 5000.0, 800.0, "3-3", null);
        AircraftModelViewDTO responseDto = new AircraftModelViewDTO(modelName, "BOEING", 200, 20000.0, 5000.0, 800.0, "url", "3-3", null);

        when(aircraftService.updateModelSpecifications(eq(modelName), any())).thenReturn(responseDto);
        ResponseEntity<EntityModel<AircraftModelViewDTO>> response = aircraftController.updateModelSpecifications(modelName, requestDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturn200OkWhenGettingCompatibleRoutes() {
        String regNum = "CS-TPA";
        RouteViewDTO route1 = new RouteViewDTO("RT-OPOLIS", "OPO", "LIS", "ACTIVE", 100);
        Page<RouteViewDTO> mockPage = new PageImpl<>(List.of(route1));

        when(aircraftService.getCompatibleRoutesForAircraft(eq(regNum), any())).thenReturn(mockPage);

        PagedResourcesAssembler<RouteViewDTO> assemblerMock = Mockito.mock(PagedResourcesAssembler.class);
        PagedModel<EntityModel<RouteViewDTO>> pagedModel = PagedModel.of(List.of(EntityModel.of(route1)), new PagedModel.PageMetadata(1, 0, 1));

        when(assemblerMock.toModel(any(Page.class), any(RepresentationModelAssembler.class))).thenReturn(pagedModel);

        ResponseEntity<PagedModel<EntityModel<RouteViewDTO>>> response =
                (ResponseEntity<PagedModel<EntityModel<RouteViewDTO>>>) (ResponseEntity<?>)
                        aircraftController.getCompatibleRoutes(regNum, PageRequest.of(0, 5), assemblerMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}