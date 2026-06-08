package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.AirportService;
import pt.isep.psoft.aisafe.application.DTO.AirportViewDTO;
import pt.isep.psoft.aisafe.application.DTO.RegisterAirportDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AirportRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportService airportService;

    @Test
    void shouldRegisterAirportSuccessfully() {

        // O construtor foi atualizado com List.of() para facilities e null para a imagem
        RegisterAirportDTO dto = new RegisterAirportDTO(
                "OPO", "Sá Carneiro", "Porto", "Portugal", "Europe/Lisbon", "INTERNATIONAL",
                41.23, -8.67, List.of(), List.of(), null
        );

        Airport mockAirport = mock(Airport.class);

        when(mockAirport.getIataCode()).thenReturn(new IATACode("OPO"));
        when(mockAirport.getName()).thenReturn("Sá Carneiro");
        when(mockAirport.getCity()).thenReturn("Porto");
        when(mockAirport.getCountry()).thenReturn("Portugal");
        when(mockAirport.getTimezone()).thenReturn("Europe/Lisbon");

        when(mockAirport.getType()).thenReturn(AirportType.INTERNATIONAL);
        when(mockAirport.getStatus()).thenReturn(AirportStatus.OPERATIONAL);

        when(mockAirport.getRunways()).thenReturn(List.of());
        when(mockAirport.getCertifications()).thenReturn(List.of());

        // Mock the new lists to prevent null pointers during DTO mapping in the service
        when(mockAirport.getFacilities()).thenReturn(List.of());

        when(airportRepository.save(any(Airport.class))).thenReturn(mockAirport);

        AirportViewDTO result = airportService.registerAirport(dto);

        assertNotNull(result);
        assertEquals("OPO", result.iataCode());
        assertEquals("Porto", result.city());

        verify(airportRepository, times(1)).save(any(Airport.class));
    }
}