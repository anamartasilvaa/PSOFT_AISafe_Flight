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
import pt.isep.psoft.aisafe.repositories.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

    @Mock private AircraftRepository aircraftRepository;
    @Mock private AircraftModelRepository modelRepository;
    @Mock private RouteRepository routeRepository;
    @Mock private ScheduledFlightRepository scheduledFlightRepository;

    @InjectMocks
    private AircraftService aircraftService;

    @Test
    void shouldSearchAircraftsByManufacturingYear() {
        Aircraft mockAircraft = mock(Aircraft.class);
        AircraftModel mockModel = mock(AircraftModel.class);

        when(mockModel.getModelName()).thenReturn(new ModelName("A320neo"));

        when(mockAircraft.getRegistrationNumber()).thenReturn(new RegistrationNumber("CS-TPA"));
        when(mockAircraft.getManufacturingDate()).thenReturn(LocalDate.of(2020, 1, 1));
        when(mockAircraft.getStatus()).thenReturn(AircraftStatus.ACTIVE);
        when(mockAircraft.getAircraftModel()).thenReturn(mockModel);

        when(aircraftRepository.findByManufacturingYear(2020)).thenReturn(List.of(mockAircraft));

        List<AircraftViewDTO> results = aircraftService.searchAircrafts(null, null, 2020);
        assertNotNull(results);
    }

    @Test
    void shouldTriggerSwapAlgorithmWhenAircraftGoesUnderMaintenance() {
        // Arrange
        RegistrationNumber brokenRegNum = new RegistrationNumber("CS-TPA");
        RegistrationNumber replacementRegNum = new RegistrationNumber("CS-REPL");

        AircraftModel mockModel = mock(AircraftModel.class);
        when(mockModel.getModelName()).thenReturn(new ModelName("A320neo"));

        Aircraft brokenAircraft = mock(Aircraft.class);
        when(brokenAircraft.getRegistrationNumber()).thenReturn(brokenRegNum);
        when(brokenAircraft.getStatus()).thenReturn(AircraftStatus.ACTIVE);
        when(brokenAircraft.getAircraftModel()).thenReturn(mockModel);

        Aircraft replacementAircraft = mock(Aircraft.class);
        when(replacementAircraft.getRegistrationNumber()).thenReturn(replacementRegNum);
        when(replacementAircraft.getAircraftModel()).thenReturn(mockModel);
        when(replacementAircraft.getStatus()).thenReturn(AircraftStatus.ACTIVE);

        ScheduledFlight endangeredFlight = mock(ScheduledFlight.class);
        when(endangeredFlight.getStatus()).thenReturn(FlightStatus.SCHEDULED);
        when(endangeredFlight.getScheduledDateTime()).thenReturn(java.time.LocalDateTime.now().plusDays(1));

        when(aircraftRepository.findByRegistrationNumber(any())).thenReturn(Optional.of(brokenAircraft));
        when(aircraftRepository.findAll()).thenReturn(List.of(brokenAircraft, replacementAircraft));

        // Avião avariado tem o voo em risco
        when(scheduledFlightRepository.findByAircraft_RegistrationNumber(brokenRegNum))
                .thenReturn(List.of(endangeredFlight));

        // Avião substituto está completamente livre
        when(scheduledFlightRepository.findByAircraft_RegistrationNumber(replacementRegNum))
                .thenReturn(List.of());

        UpdateAircraftStatusDTO dto = new UpdateAircraftStatusDTO("UNDER_MAINTENANCE");

        // Act
        Map<String, Object> response = aircraftService.updateAircraftStatusWithReport(brokenRegNum.number(), dto);

        // Assert
        verify(endangeredFlight, atLeastOnce()).changeAircraft(replacementAircraft);

        assertNotNull(response);
        assertTrue(response.containsKey("swapReport"));
    }
}