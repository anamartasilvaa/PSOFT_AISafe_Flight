package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.AircraftService;
import pt.isep.psoft.aisafe.application.DTO.AircraftViewDTO;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.AircraftRepository;
import pt.isep.psoft.aisafe.repositories.AircraftModelRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {


    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository modelRepository;


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


        verify(aircraftRepository, times(1)).findByManufacturingYear(2020);
    }

    @Test
    void shouldGetTop5UtilizedModels() { // US204
        pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO dto1 =
                new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO("B737 MAX", 15000.0);
        pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO dto2 =
                new pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO("A320neo", 10000.0);

        when(aircraftRepository.findTop5ModelsByFlightHours(any())).thenReturn(List.of(dto1, dto2));

        List<pt.isep.psoft.aisafe.application.DTO.TopAircraftModelDTO> topModels =
                aircraftService.getTop5UtilizedModels("hours");

        assertEquals(2, topModels.size());
        assertEquals("B737 MAX", topModels.get(0).modelName());
        assertEquals(15000.0, topModels.get(0).totalFlightHours());
    }

    @Mock
    private pt.isep.psoft.aisafe.repositories.RouteRepository routeRepository;

    @Test
    void shouldGetCompatibleRoutesForAircraft() { // US203
        String regNum = "CS-TPA";

        Aircraft mockAircraft = mock(Aircraft.class);
        AircraftModel mockModel = mock(AircraftModel.class);

        when(mockAircraft.getAircraftModel()).thenReturn(mockModel);
        when(mockModel.getMaximumRange()).thenReturn(5000.0);
        when(mockAircraft.getActualSeatingCapacity()).thenReturn(150);

        when(aircraftRepository.findByRegistrationNumber(new RegistrationNumber(regNum)))
                .thenReturn(java.util.Optional.of(mockAircraft));

        Route mockRoute = mock(Route.class);
        when(mockRoute.getRouteId()).thenReturn(new RouteId("RT-123"));
        when(mockRoute.getStatus()).thenReturn(RouteStatus.ACTIVE);
        when(mockRoute.getMinimumCapacity()).thenReturn(100);

        Airport originMock = mock(Airport.class);
        when(originMock.getIataCode()).thenReturn(new IATACode("OPO"));
        when(mockRoute.getOrigin()).thenReturn(originMock);

        Airport destMock = mock(Airport.class);
        when(destMock.getIataCode()).thenReturn(new IATACode("LIS"));
        when(mockRoute.getDestination()).thenReturn(destMock);

        org.springframework.data.domain.Page<Route> mockPage =
                new org.springframework.data.domain.PageImpl<>(List.of(mockRoute));

        when(routeRepository.findCompatibleRoutes(eq(5000.0), eq(150), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(mockPage);

        org.springframework.data.domain.Page<pt.isep.psoft.aisafe.application.DTO.RouteViewDTO> result =
                aircraftService.getCompatibleRoutesForAircraft(regNum, org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("RT-123", result.getContent().get(0).routeId());
        assertEquals("OPO", result.getContent().get(0).originIata());
        assertEquals("LIS", result.getContent().get(0).destinationIata());
    }
};