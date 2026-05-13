package pt.isep.psoft.aisafe.domain;

import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

@Embeddable
public record IATACode(String code) {
    public IATACode {
        // Regra 1: Não pode ser nulo nem vazio
        Assert.hasText(code, "IATA code must not be blank.");

        // Regra 2: Tem de ter exatamente 3 letras maiúsculas
        Assert.isTrue(code.matches("^[A-Z]{3}$"),
                "IATA code must be exactly 3 uppercase letters.");
    }
}