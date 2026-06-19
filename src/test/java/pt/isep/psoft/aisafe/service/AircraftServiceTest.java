package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.AircraftViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateAircraftStatusDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;
import pt.isep.psoft.aisafe.repositories.ScheduledFlightRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository modelRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private ScheduledFlightRepository scheduledFlightRepository;

    @InjectMocks
    private AircraftService aircraftService;

    @Test
    void shouldSearchAircraftsByManufacturingYear() {
        Aircraft mockAircraft = mock(Aircraft.class);
        AircraftModel mockModel = mock(AircraftModel.class);

        when(mockAircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(mockAircraft.getManufacturingDate()).thenReturn(LocalDate.of(2020, 1, 1));
        when(mockAircraft.getActualSeatingCapacity()).thenReturn(180);
        when(mockAircraft.getStatus()).thenReturn(AircraftStatus.ACTIVE);
        when(mockAircraft.getAircraftModel()).thenReturn(mockModel);
        when(mockModel.getModelName()).thenReturn(new ModelName("A320"));
        when(mockModel.getModelPhotoUrl()).thenReturn("url");

        when(aircraftRepository.findByManufacturingYear(2020)).thenReturn(List.of(mockAircraft));

        List<AircraftViewDTO> results = aircraftService.searchAircrafts(null, null, 2020);

        assertEquals(1, results.size());
        assertEquals("CS-TPA", results.get(0).registrationNumber());
        assertEquals("A320", results.get(0).modelName());
    }

    @Test
    void shouldTriggerSwapAlgorithmWhenAircraftGoesUnderMaintenance() {
        // Arrange
        String regNum = "CS-TPA";
        String replacementRegNum = "CS-TVC";

        // Mocks do Modelo
        AircraftModel sharedModel = mock(AircraftModel.class);
        when(sharedModel.getModelName()).thenReturn(new ModelName("A320"));
        when(sharedModel.getModelPhotoUrl()).thenReturn("url");

        // Avião que vai avariar
        Aircraft brokenAircraft = mock(Aircraft.class);
        RegistrationNumber brokenReg = new RegistrationNumber(regNum);
        when(brokenAircraft.getRegistrationNumber()).thenReturn(brokenReg);
        when(brokenAircraft.getAircraftModel()).thenReturn(sharedModel);
        // Garantimos que ele tem status para o mapToViewDTO não explodir
        when(brokenAircraft.getStatus()).thenReturn(AircraftStatus.ACTIVE);
        when(brokenAircraft.getManufacturingDate()).thenReturn(LocalDate.now());

        // Avião Salvador
        Aircraft saviorAircraft = mock(Aircraft.class);
        RegistrationNumber saviorReg = new RegistrationNumber(replacementRegNum);
        when(saviorAircraft.getRegistrationNumber()).thenReturn(saviorReg);
        when(saviorAircraft.getAircraftModel()).thenReturn(sharedModel);
        when(saviorAircraft.getStatus()).thenReturn(AircraftStatus.ACTIVE);

        // O Voo em perigo
        ScheduledFlight endangeredFlight = mock(ScheduledFlight.class);
        when(endangeredFlight.getStatus()).thenReturn(FlightStatus.SCHEDULED);
        when(endangeredFlight.getScheduledDateTime()).thenReturn(java.time.LocalDateTime.now().plusDays(2));

        // Configuração dos Repositórios
        when(aircraftRepository.findByRegistrationNumber(brokenReg)).thenReturn(Optional.of(brokenAircraft));
        when(aircraftRepository.findAll()).thenReturn(List.of(brokenAircraft, saviorAircraft));
        when(scheduledFlightRepository.findByAircraft_RegistrationNumber(brokenReg)).thenReturn(List.of(endangeredFlight));
        when(scheduledFlightRepository.findByAircraft_RegistrationNumber(saviorReg)).thenReturn(List.of());

        UpdateAircraftStatusDTO dto = new UpdateAircraftStatusDTO("UNDER_MAINTENANCE");

        // Act
        Map<String, Object> response = aircraftService.updateAircraftStatusWithReport(regNum, dto);

        // Assert
        verify(brokenAircraft, times(1)).updateStatus(AircraftStatus.UNDER_MAINTENANCE);
        verify(endangeredFlight, times(1)).changeAircraft(saviorAircraft);

        List<String> report = (List<String>) response.get("swapReport");
        assertNotNull(report);
        assertTrue(report.stream().anyMatch(msg -> msg.contains("RECOVERED")));
    }
}