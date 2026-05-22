package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchRoutesTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService;

    private Route mockRoute;

    @BeforeEach
    void setUp() {
        mockRoute = mock(Route.class);
        RouteId mockRouteId = mock(RouteId.class);
        when(mockRouteId.id()).thenReturn("RT-OPOLIS");
        when(mockRoute.getRouteId()).thenReturn(mockRouteId);

        Airport mockAirport = mock(Airport.class);
        IATACode mockIata = mock(IATACode.class);
        when(mockIata.code()).thenReturn("OPO");
        when(mockAirport.getIataCode()).thenReturn(mockIata);

        when(mockRoute.getOrigin()).thenReturn(mockAirport);
        when(mockRoute.getDestination()).thenReturn(mockAirport);
        when(mockRoute.getStatus()).thenReturn(RouteStatus.ACTIVE);
    }

    @Test
    void shouldSearchRoutesByBothOriginAndDestination() {
        when(routeRepository.findByOrigin_IataCode_CodeAndDestination_IataCode_Code("OPO", "LIS"))
                .thenReturn(List.of(mockRoute));

        List<RouteViewDTO> results = routeService.searchRoutes("OPO", "LIS");

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(routeRepository, times(1)).findByOrigin_IataCode_CodeAndDestination_IataCode_Code("OPO", "LIS");
    }

    @Test
    void shouldSearchRoutesByDestinationOnly() {
        when(routeRepository.findByDestination_IataCode_Code("LIS")).thenReturn(List.of(mockRoute));

        List<RouteViewDTO> results = routeService.searchRoutes(null, "LIS");

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(routeRepository, times(1)).findByDestination_IataCode_Code("LIS");
    }
}