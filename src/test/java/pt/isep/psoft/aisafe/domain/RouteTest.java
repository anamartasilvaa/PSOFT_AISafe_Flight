package pt.isep.psoft.aisafe.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RouteTest {

    @Test
    void ensureOriginAndDestinationCannotBeTheSame() {
        // Criamos um Aeroporto fictício
        Airport sameAirport = mock(Airport.class);
        RouteId routeId = new RouteId("R001");

        // Testamos se o sistema bloqueia a criação de uma rota para o próprio aeroporto
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Route(routeId, sameAirport, sameAirport, 120, 500.0, 150);
        });

        assertEquals("Origin and destination airports must be different.", exception.getMessage());
    }

    @Test
    void ensureValidRouteIsCreated() {
        // Criamos dois Aeroportos fictícios diferentes
        Airport origin = mock(Airport.class);
        Airport destination = mock(Airport.class);
        RouteId routeId = new RouteId("R001");

        // Criamos a Rota
        Route route = new Route(routeId, origin, destination, 120, 500.0, 150);

        // Verificamos se foi criada com sucesso e se o estado inicial é ACTIVE
        assertNotNull(route);
        assertEquals(RouteStatus.ACTIVE, route.getStatus());
        assertEquals(120, route.getEstimatedFlightTime());
    }
}