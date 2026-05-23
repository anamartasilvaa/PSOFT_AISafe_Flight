package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.DTO.CreateRouteDTO;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.repositories.AirportRepository;
import pt.isep.psoft.aisafe.repositories.RouteHistoryRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;



import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateRouteTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private AirportRepository airportRepository;
    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @InjectMocks
    private RouteService routeService;

    private Airport mockOrigin;
    private Airport mockDestination;
    private Route mockRoute;

    @BeforeEach
    void setUp() {
        mockOrigin = mock(Airport.class);
        mockDestination = mock(Airport.class);
        mockRoute = mock(Route.class);

        IATACode mockIataOpo = mock(IATACode.class);
        when(mockIataOpo.code()).thenReturn("OPO");
        when(mockOrigin.getIataCode()).thenReturn(mockIataOpo);

        IATACode mockIataLis = mock(IATACode.class);
        when(mockIataLis.code()).thenReturn("LIS");
        when(mockDestination.getIataCode()).thenReturn(mockIataLis);

        RouteId mockRouteId = mock(RouteId.class);
        when(mockRouteId.id()).thenReturn("RT-OPOLIS");
        when(mockRoute.getRouteId()).thenReturn(mockRouteId);
        when(mockRoute.getOrigin()).thenReturn(mockOrigin);
        when(mockRoute.getDestination()).thenReturn(mockDestination);
        when(mockRoute.getStatus()).thenReturn(RouteStatus.ACTIVE);
    }

    @Test
    void shouldCreateRouteSuccessfullyWhenAirportsExist() {
        CreateRouteDTO dto = new CreateRouteDTO("RT-OPOLIS", "OPO", "LIS", 45, 300.0, 150);

        when(airportRepository.findByIataCodeString("OPO")).thenReturn(Optional.of(mockOrigin));
        when(airportRepository.findByIataCodeString("LIS")).thenReturn(Optional.of(mockDestination));
        when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);

        RouteViewDTO result = routeService.createRoute(dto);

        assertNotNull(result);
        assertEquals("RT-OPOLIS", result.routeId());
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    @Test
    void shouldThrowExceptionWhenOriginAirportDoesNotExist() {
        CreateRouteDTO dto = new CreateRouteDTO("RT-OPOLIS", "FAKE", "LIS", 45, 300.0, 150);

        when(airportRepository.findByIataCodeString("FAKE")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            routeService.createRoute(dto);
        });

        assertEquals("Origin Airport not found: FAKE", ex.getMessage());
        verify(routeRepository, never()).save(any());
    }
}