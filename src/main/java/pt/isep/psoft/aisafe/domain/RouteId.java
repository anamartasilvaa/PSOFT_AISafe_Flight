package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record RouteId(String id) {


    public RouteId {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Route ID cannot be null or empty!");
        }
    }


    protected RouteId() {
        this(null);
    }
}