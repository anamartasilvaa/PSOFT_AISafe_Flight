package pt.isep.psoft.aisafe;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.aisafe.domain.RouteId;

import static org.junit.jupiter.api.Assertions.*;

class RouteIdTest {

    @Test
    void ensureRouteIdCannotBeNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RouteId(null);
        });
        assertEquals("Route ID cannot be blank.", exception.getMessage());
    }

    @Test
    void ensureRouteIdCannotBeEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RouteId("   ");
        });
        assertEquals("Route ID cannot be blank.", exception.getMessage());
    }

    @Test
    void ensureValidRouteIdCreatesSuccessfully() {
        RouteId routeId = new RouteId("LIS-OPO-001");
        assertEquals("LIS-OPO-001", routeId.id());
    }
}