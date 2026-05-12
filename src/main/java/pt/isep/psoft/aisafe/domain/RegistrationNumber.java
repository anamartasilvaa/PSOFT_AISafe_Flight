package pt.isep.psoft.aisafe.domain;

import org.springframework.util.Assert;

public record RegistrationNumber(String number) {

    public RegistrationNumber {
        // Regra 1: Não pode ser vazio
        Assert.hasText(number, "Registration number must not be blank.");

        // Regra 2: Um formato básico de matrícula de avião (ex: CS-TPA ou N-12345)
        Assert.isTrue(number.matches("^[A-Z0-9]{1,5}-[A-Z0-9]{1,5}$"),
                "Registration number must follow a valid format (e.g., CS-TPA).");
    }

    @Override
    public String toString() {
        return this.number; // Usa o nome da variável que tens aí dentro, deve ser 'number' ou parecida
    }
}