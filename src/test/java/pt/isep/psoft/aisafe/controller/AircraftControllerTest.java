package pt.isep.psoft.aisafe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.isep.psoft.aisafe.api.AircraftController;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.*;

import java.time.LocalDate;
import java.util.List;

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
    void shouldReturn200OkWhenUpdatingModelImage() throws Exception { // US202
        String modelName = "B737 MAX";

        org.springframework.mock.web.MockMultipartFile mockFile =
                new org.springframework.mock.web.MockMultipartFile(
                        "file", "plane.png", "image/png", "dummy image content".getBytes()
                );

        pt.isep.psoft.aisafe.application.DTO.AircraftModelViewDTO responseDto =
                new pt.isep.psoft.aisafe.application.DTO.AircraftModelViewDTO(
                        modelName, "BOEING", 200, 20000.0, 5000.0, 800.0, "/uploads/plane.png", "3-3", null
                );

        when(aircraftService.updateModelImage(eq(modelName), any(org.springframework.web.multipart.MultipartFile.class)))
                .thenReturn(responseDto);

        ResponseEntity<EntityModel<pt.isep.psoft.aisafe.application.DTO.AircraftModelViewDTO>> response =
                aircraftController.updateModelImage(modelName, mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("/uploads/plane.png", response.getBody().getContent().modelPhotoUrl());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }

    @Test
    void shouldReturn200OkWhenGettingTop5Models() { // US204
        pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO top1 = new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO("A320neo", 15000.0);
        pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO top2 = new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO("B737 MAX", 10000.0);

        when(aircraftService.getTop5UtilizedModels("hours")).thenReturn(java.util.List.of(top1, top2));

        ResponseEntity<org.springframework.hateoas.CollectionModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO>>> response =
                aircraftController.getTop5Models("hours");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertTrue(response.getBody().hasLinks(), "Collection should contain a self link");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturn200OkWhenGettingCompatibleRoutes() { // US203
        String regNum = "CS-TPA";
        pt.isep.psoft.aisafe.application.DTO.RouteViewDTO route1 = new pt.isep.psoft.aisafe.application.DTO.RouteViewDTO("RT-OPOLIS", "OPO", "LIS", "ACTIVE", 100);

        // 1. Simular a Página
        org.springframework.data.domain.Page<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO> mockPage =
                new org.springframework.data.domain.PageImpl<>(java.util.List.of(route1));

        when(aircraftService.getCompatibleRoutesForAircraft(eq(regNum), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        // 2. Mock do Assembler
        org.springframework.data.web.PagedResourcesAssembler assemblerMock =
                org.mockito.Mockito.mock(org.springframework.data.web.PagedResourcesAssembler.class);

        // 3. Simular o PagedModel final
        org.springframework.hateoas.PagedModel pagedModel = org.springframework.hateoas.PagedModel.of(
                java.util.List.of(EntityModel.of(route1)),
                new org.springframework.hateoas.PagedModel.PageMetadata(1, 0, 1)
        );

        // 4. A MAGIA AQUI: Dizer explicitamente que o segundo 'any' é um RepresentationModelAssembler!
        when(assemblerMock.toModel(
                any(org.springframework.data.domain.Page.class),
                any(org.springframework.hateoas.server.RepresentationModelAssembler.class)
        )).thenReturn(pagedModel);

        // 5. Executar o teste
        ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO>>> response =
                (ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO>>>) (ResponseEntity<?>)
                        aircraftController.getCompatibleRoutes(regNum, org.springframework.data.domain.PageRequest.of(0, 5), assemblerMock);

        // 6. Validações
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void shouldReturn200OkWhenGettingRealTimeStatus() { // US205
        String regNum = "CS-TPA";

        // Ensinar o Mock a devolver "in-flight"
        when(aircraftService.getRealTimeAircraftStatus(eq(regNum))).thenReturn("in-flight");

        // Chamar o Controller
        ResponseEntity<EntityModel<pt.isep.psoft.aisafe.application.DTO.RealTimeStatusDTO>> response =
                aircraftController.getRealTimeStatus(regNum);

        // Validações
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("in-flight", response.getBody().getContent().realTimeStatus());
        assertEquals(regNum, response.getBody().getContent().registrationNumber());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturn200OkWhenGettingAllOperationalHours() { // US206 (Frota Completa)
        OperationalHoursDTO dto = new OperationalHoursDTO("CS-TPA", 12500.5);

        org.springframework.data.domain.Page<OperationalHoursDTO> mockPage = new org.springframework.data.domain.PageImpl<>(List.of(dto));
        when(aircraftService.getAllAircraftOperationalHours(any(org.springframework.data.domain.Pageable.class))).thenReturn(mockPage);

        org.springframework.data.web.PagedResourcesAssembler assemblerMock = Mockito.mock(org.springframework.data.web.PagedResourcesAssembler.class);
        org.springframework.hateoas.PagedModel pagedModel = org.springframework.hateoas.PagedModel.of(
                List.of(EntityModel.of(dto)), new org.springframework.hateoas.PagedModel.PageMetadata(1, 0, 1)
        );

        when(assemblerMock.toModel(any(org.springframework.data.domain.Page.class), any(org.springframework.hateoas.server.RepresentationModelAssembler.class))).thenReturn(pagedModel);

        ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<OperationalHoursDTO>>> response =
                (ResponseEntity<org.springframework.hateoas.PagedModel<EntityModel<OperationalHoursDTO>>>) (ResponseEntity<?>)
                        aircraftController.getAllOperationalHours(org.springframework.data.domain.PageRequest.of(0, 10), assemblerMock);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}