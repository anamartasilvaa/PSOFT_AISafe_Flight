package pt.isep.psoft.aisafe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pt.isep.psoft.aisafe.application.DTO.AlternativeRouteDTO;
import pt.isep.psoft.aisafe.application.FastestRouteStrategy;
import pt.isep.psoft.aisafe.domain.*;
import pt.isep.psoft.aisafe.repositories.RouteRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FastestRouteStrategyTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private FastestRouteStrategy strategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFindAlternativesAndSortThemWithMCTPenalty() {
        // Arrange - Criar os mocks das rotas
        Route directRoute = createMockRoute("LIS", "MAD", 140);
        Route leg1 = createMockRoute("LIS", "OPO", 45);
        Route leg2 = createMockRoute("OPO", "MAD", 80);

        // Simular o comportamento do repositório
        when(routeRepository.findByOrigin_IataCode_Code(eq("LIS"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(directRoute, leg1)));

        when(routeRepository.findByOrigin_IataCode_Code(eq("OPO"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(leg2)));

        when(routeRepository.findByOrigin_IataCode_Code(eq("MAD"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // Act - Correr o algoritmo
        List<AlternativeRouteDTO> results = strategy.findAlternatives("LIS", "MAD");

        // Assert - Verificar os resultados
        assertEquals(2, results.size(), "Deve encontrar 2 alternativas (uma direta, uma com escala)");

        // A Rota 1 (Direta) deve demorar 140 minutos e estar em primeiro
        assertEquals(140, results.get(0).totalEstimatedFlightTime());
        assertEquals(1, results.get(0).legs().size());

        // A Rota 2 (Com escala) deve demorar: 45 + 60 (MCT) + 80 = 185 minutos e estar em segundo
        assertEquals(185, results.get(1).totalEstimatedFlightTime());
        assertEquals(2, results.get(1).legs().size());
    }

    private Route createMockRoute(String origin, String dest, int duration) {
        Route r = mock(Route.class);
        Airport o = mock(Airport.class);
        Airport d = mock(Airport.class);
        IATACode oCode = mock(IATACode.class);
        IATACode dCode = mock(IATACode.class);
        RouteId rId = mock(RouteId.class);

        when(oCode.code()).thenReturn(origin);
        when(dCode.code()).thenReturn(dest);
        when(o.getIataCode()).thenReturn(oCode);
        when(d.getIataCode()).thenReturn(dCode);

        when(r.getOrigin()).thenReturn(o);
        when(r.getDestination()).thenReturn(d);
        when(r.getEstimatedFlightTime()).thenReturn(duration);
        when(r.getStatus()).thenReturn(RouteStatus.ACTIVE);
        when(r.getRouteId()).thenReturn(rId);
        when(rId.id()).thenReturn("RT-" + origin + dest);

        return r;
    }
}