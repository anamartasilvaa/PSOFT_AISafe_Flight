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

        when(mockAirport.getFacilities()).thenReturn(List.of());

        when(airportRepository.save(any(Airport.class))).thenReturn(mockAirport);

        AirportViewDTO result = airportService.registerAirport(dto);

        assertNotNull(result);
        assertEquals("OPO", result.iataCode());
        assertEquals("Porto", result.city());

        verify(airportRepository, times(1)).save(any(Airport.class));
    }

    @Test
    void shouldUpdateAirportImageSuccessfully() throws java.io.IOException { // US207
        String iataCode = "OPO";
        Airport mockAirport = mock(Airport.class);
        when(airportRepository.findByIataCodeString(iataCode)).thenReturn(java.util.Optional.of(mockAirport));
        when(airportRepository.save(any(Airport.class))).thenReturn(mockAirport);

        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("map.png");
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("fake bytes".getBytes()));

        when(mockAirport.getIataCode()).thenReturn(new IATACode(iataCode));
        when(mockAirport.getType()).thenReturn(AirportType.INTERNATIONAL);
        when(mockAirport.getStatus()).thenReturn(AirportStatus.OPERATIONAL);

        AirportViewDTO result = airportService.updateAirportImage(iataCode, mockFile);

        assertNotNull(result);
        verify(mockAirport, times(1)).updateImage(anyString());
        verify(airportRepository, times(1)).save(mockAirport);
    }

    @Test
    void shouldGroupAirportsByCountry() { // US211
        Airport mockAirport1 = mock(Airport.class);
        when(mockAirport1.getCountry()).thenReturn("Portugal");
        when(mockAirport1.getIataCode()).thenReturn(new IATACode("OPO"));
        when(mockAirport1.getType()).thenReturn(AirportType.INTERNATIONAL);
        when(mockAirport1.getStatus()).thenReturn(AirportStatus.OPERATIONAL);

        Airport mockAirport2 = mock(Airport.class);
        when(mockAirport2.getCountry()).thenReturn("Spain");
        when(mockAirport2.getIataCode()).thenReturn(new IATACode("MAD"));
        when(mockAirport2.getType()).thenReturn(AirportType.INTERNATIONAL);
        when(mockAirport2.getStatus()).thenReturn(AirportStatus.OPERATIONAL);

        when(airportRepository.findAll()).thenReturn(List.of(mockAirport1, mockAirport2));


        Object result = airportService.getAirportsGrouped("country");

        assertNotNull(result);
        assertTrue(result instanceof java.util.Map);
        java.util.Map<?, ?> groupedMap = (java.util.Map<?, ?>) result;
        assertTrue(groupedMap.containsKey("Portugal"));
        assertTrue(groupedMap.containsKey("Spain"));
    }

    //US225
    @Test
    void givenValidCsvFile_whenImportAirports_thenSuccessfullyParsesAndSaves() {
        String csvContent = "IATACode,Name,City,Country,Region,Timezone,Type,latitude,longitude\n" +
                "OPO,Francisco Sa Carneiro,Porto,Portugal,Norte,Europe/Lisbon,INTERNATIONAL,41.23,-8.67\n";

        org.springframework.mock.web.MockMultipartFile fakeCsvFile = new org.springframework.mock.web.MockMultipartFile(
                "file", "aeroportos.csv", "text/csv", csvContent.getBytes()
        );

        lenient().when(airportRepository.findByIataCodeString(anyString()))
                .thenReturn(java.util.Optional.empty());
        lenient().when(airportRepository.save(any(Airport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<AirportViewDTO> importedAirports = airportService.importAirportsFromCsv(fakeCsvFile);

        assertNotNull(importedAirports);
        assertFalse(importedAirports.isEmpty(), "A lista de aeroportos importados não devia estar vazia!");
        assertEquals("OPO", importedAirports.get(0).iataCode());

        verify(airportRepository, atLeastOnce()).save(any(Airport.class));
    }

    // US225: Lançar erro quando o ficheiro está vazio
    @Test
    void givenEmptyCsvFile_whenImportAirports_thenThrowsException() {
        org.springframework.mock.web.MockMultipartFile emptyFile = new org.springframework.mock.web.MockMultipartFile(
                "file", "vazio.csv", "text/csv", "".getBytes()
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            airportService.importAirportsFromCsv(emptyFile);
        });

        assertNotNull(exception.getMessage());
    }
}