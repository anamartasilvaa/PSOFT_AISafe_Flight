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
import pt.isep.psoft.aisafe.api.AirportController;
import pt.isep.psoft.aisafe.application.AirportService;
import pt.isep.psoft.aisafe.application.DTO.AirportViewDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAirportDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAirportDetailsDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AirportControllerTest {

    @Mock
    private AirportService airportService;

    @InjectMocks
    private AirportController airportController;

    @BeforeEach
    void setUp() {
        // Truque para o HATEOAS (linkTo)
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldReturn201CreatedWhenCreatingAirport() {
        // Arrange (Adicionados List.of(), null para as facilities e imageUrl)
        RegisterAirportDTO requestDto = new RegisterAirportDTO("OPO", "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL", 41.23, -8.67, List.of(), List.of(), null);

        // AirportViewDTO agora tem 13 parâmetros
        AirportViewDTO responseDto = new AirportViewDTO("OPO", "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL", "OPERATIONAL", List.of(), List.of(), List.of(), null, null, null);

        when(airportService.registerAirport(any(RegisterAirportDTO.class))).thenReturn(responseDto);

        // Act
        ResponseEntity<EntityModel<AirportViewDTO>> response = airportController.createAirport(requestDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OPO", response.getBody().getContent().iataCode());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }

    @Test
    void shouldReturn200OkWhenGettingAirport() {
        // Arrange
        String iataCode = "OPO";
        // AirportViewDTO com 13 parâmetros
        AirportViewDTO responseDto = new AirportViewDTO(iataCode, "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL", "OPERATIONAL", List.of(), List.of(), List.of(), null, null, null);

        when(airportService.getAirportByIataCode(eq(iataCode))).thenReturn(responseDto);

        // Act
        ResponseEntity<EntityModel<AirportViewDTO>> response = airportController.getAirport(iataCode);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(iataCode, response.getBody().getContent().iataCode());
    }

    @Test
    void shouldReturn200OkWhenUpdatingAirportStatus() {
        // Arrange
        String iataCode = "OPO";
        String newStatus = "CLOSED";
        // AirportViewDTO com 13 parâmetros
        AirportViewDTO responseDto = new AirportViewDTO(iataCode, "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL", "CLOSED", List.of(), List.of(), List.of(), null, null, null);

        when(airportService.updateAirportStatus(eq(iataCode), eq(newStatus))).thenReturn(responseDto);

        // Act
        ResponseEntity<EntityModel<AirportViewDTO>> response = airportController.updateStatus(iataCode, newStatus);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CLOSED", response.getBody().getContent().status());
    }

    @Test
    void shouldReturn200OkWhenUpdatingAirportImage() throws Exception { // US207a
        String iataCode = "OPO";

        org.springframework.mock.web.MockMultipartFile mockFile =
                new org.springframework.mock.web.MockMultipartFile(
                        "file", "airport.jpg", "image/jpeg", "fake image bytes".getBytes()
                );

        AirportViewDTO responseDto = new AirportViewDTO(
                iataCode, "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL", "OPERATIONAL",
                java.util.List.of(), java.util.List.of(), java.util.List.of(), "/uploads/OPO_airport.jpg", null, null
        );

        when(airportService.updateAirportImage(eq(iataCode), any(org.springframework.web.multipart.MultipartFile.class)))
                .thenReturn(responseDto);

        ResponseEntity<EntityModel<AirportViewDTO>> response = airportController.updateAirportImage(iataCode, mockFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("/uploads/OPO_airport.jpg", response.getBody().getContent().imageUrl());
    }

    @Test
    void shouldReturn200OkWhenUpdatingAirportDetails() { // US208
        // Arrange
        String iataCode = "OPO";
        UpdateAirportDetailsDTO requestDto = new UpdateAirportDetailsDTO("24/7", "info@aeroportoporto.pt");

        AirportViewDTO responseDto = new AirportViewDTO(
                iataCode, "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL", "OPERATIONAL",
                List.of(), List.of(), List.of(), "imageUrl", "24/7", "info@aeroportoporto.pt"
        );

        when(airportService.updateAirportDetails(eq(iataCode), any(UpdateAirportDetailsDTO.class))).thenReturn(responseDto);

        // Act
        ResponseEntity<EntityModel<AirportViewDTO>> response = airportController.updateAirportDetails(iataCode, requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("24/7", response.getBody().getContent().operationalHours());
        assertEquals("info@aeroportoporto.pt", response.getBody().getContent().contactInformation());
        assertTrue(response.getBody().hasLinks(), "Response should contain HATEOAS links");
    }

    @Test
    void shouldReturn200OkWhenGettingAirportsGrouped() { // US211
        java.util.Map<String, java.util.List<pt.isep.psoft.aisafe.application.DTO.AirportViewDTO>> mockResponse = new java.util.HashMap<>();

        pt.isep.psoft.aisafe.application.DTO.AirportViewDTO airport = new pt.isep.psoft.aisafe.application.DTO.AirportViewDTO(
                "OPO", "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL", "OPERATIONAL",
                java.util.List.of(), java.util.List.of(), java.util.List.of(), null, null, null
        );

        mockResponse.put("Portugal", java.util.List.of(airport));

        when(airportService.getAirportsGrouped("country")).thenReturn(mockResponse);

        ResponseEntity<Object> response = airportController.getAirportsGrouped("country");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}