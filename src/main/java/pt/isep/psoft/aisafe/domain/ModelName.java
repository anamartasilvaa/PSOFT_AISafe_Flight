package pt.isep.psoft.aisafe.domain;

import org.springframework.util.Assert;

public record ModelName(String name) {
    public ModelName {
        Assert.hasText(name, "The model name cannot be empty.");
    }

    @Override
    public String toString() {
        return this.name; // Usa o nome da variável que guarda o nome do modelo
    }
}