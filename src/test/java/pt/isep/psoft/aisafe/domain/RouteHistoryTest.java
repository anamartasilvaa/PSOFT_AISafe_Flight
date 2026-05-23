package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.aisafe.application.RouteService;
import pt.isep.psoft.aisafe.application.DTO.RouteHistoryDTO;
import pt.isep.psoft.aisafe.repositories.RouteHistoryRepository;

import java.time.LocalDateTime;
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
        when(mockHistory1.getRouteId()).thenReturn(routeId);
        when(mockHistory1.getAction()).thenReturn("UPDATED");
        when(mockHistory1.getStartDate()).thenReturn(LocalDateTime.now());
        when(mockHistory1.getEndDate()).thenReturn(null);

        RouteHistory mockHistory2 = mock(RouteHistory.class);
        when(mockHistory2.getRouteId()).thenReturn(routeId);
        when(mockHistory2.getAction()).thenReturn("CREATED");
        when(mockHistory2.getStartDate()).thenReturn(LocalDateTime.now().minusDays(1));
        when(mockHistory2.getEndDate()).thenReturn(LocalDateTime.now());

        when(routeHistoryRepository.findByRouteIdOrderByStartDateDesc(routeId))
                .thenReturn(List.of(mockHistory1, mockHistory2));

        List<RouteHistoryDTO> history = routeService.getRouteHistory(routeId);

        assertNotNull(history);
        assertEquals(2, history.size());

        assertEquals("UPDATED", history.get(0).action());
        assertEquals("CREATED", history.get(1).action());

        verify(routeHistoryRepository, times(1)).findByRouteIdOrderByStartDateDesc(routeId);
    }
}