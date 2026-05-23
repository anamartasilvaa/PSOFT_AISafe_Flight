package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewRoutesFromAirportTest {

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
    void shouldGetRouteDetailsGivenItsId() {
        when(routeRepository.findByRouteId(any(RouteId.class))).thenReturn(Optional.of(mockRoute));

        RouteViewDTO result = routeService.getRoute("RT-OPOLIS");

        assertNotNull(result);
        assertEquals("RT-OPOLIS", result.routeId());
        verify(routeRepository, times(1)).findByRouteId(any(RouteId.class));
    }

    @Test
    void shouldViewAllRoutesFromSpecificAirport() {
        Page<Route> page = new PageImpl<>(List.of(mockRoute));

        when(routeRepository.findByOrigin_IataCode_Code(eq("OPO"), any(Pageable.class)))
                .thenReturn(page);

        var results = routeService.searchRoutes("OPO", null, Pageable.unpaged());

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        verify(routeRepository, times(1)).findByOrigin_IataCode_Code(eq("OPO"), any(Pageable.class));
    }
}