package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.DTO.RouteViewDTO;
import pt.isep.psoft.aisafe.application.DTO.UpdateRouteDTO;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.repositories.RouteHistoryRepository;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateDeactivateRouteTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RouteHistoryRepository routeHistoryRepository;

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
    void shouldUpdateRouteParameters() {
        UpdateRouteDTO dto = new UpdateRouteDTO(50, 350.0, 180);

        when(routeRepository.findByRouteId(any(RouteId.class))).thenReturn(Optional.of(mockRoute));
        when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);

        RouteViewDTO result = routeService.updateRoute("RT-OPOLIS", dto);

        assertNotNull(result);
        verify(mockRoute, times(1)).updateParameters(50, 350.0, 180);
        verify(routeRepository, times(1)).save(mockRoute);
        verify(routeHistoryRepository, times(1)).save(any(RouteHistory.class)); // Regista a atualização
    }

    @Test
    void shouldDeactivateRoute() {
        when(routeRepository.findByRouteId(any(RouteId.class))).thenReturn(Optional.of(mockRoute));
        when(routeRepository.save(any(Route.class))).thenReturn(mockRoute);

        RouteViewDTO result = routeService.deactivateRoute("RT-OPOLIS");

        assertNotNull(result);
        verify(mockRoute, times(1)).deactivate();
        verify(routeRepository, times(1)).save(mockRoute);
        verify(routeHistoryRepository, times(1)).save(any(RouteHistory.class)); // Regista a desativação
    }
}