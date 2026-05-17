package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record RouteId(

        @Column(name = "route_id")
        String id

) {

    public RouteId {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Route ID cannot be blank."
            );
        }
    }

    @Override
    public String toString() {
        return id;
    }
}