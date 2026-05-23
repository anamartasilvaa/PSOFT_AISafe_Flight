package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.repositories.RouteHistoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteHistoryTest {

    @Mock
    private RouteHistoryRepository routeHistoryRepository;

    @InjectMocks
    private RouteService routeService;

    @Test
    void shouldKeepTrackAndReturnRouteHistory() {
        String routeId = "RT-OPOLIS";
        RouteHistory mockHistory1 = mock(RouteHistory.class);
        RouteHistory mockHistory2 = mock(RouteHistory.class);

        when(routeHistoryRepository.findByRouteIdOrderByStartDateDesc(routeId))
                .thenReturn(List.of(mockHistory1, mockHistory2));

        List<RouteHistory> history = routeService.getRouteHistory(routeId);

        assertNotNull(history);
        assertEquals(2, history.size());
        verify(routeHistoryRepository, times(1)).findByRouteIdOrderByStartDateDesc(routeId);
    }
}