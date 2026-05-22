package pt.isep.psoft.aisafe.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "AISafe API", version = "v1", description = "API Documentation for the Flight Management System"),
        security = @SecurityRequirement(name = "bearerAuth") // Requires the token globally
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Insert the JWT token obtained from the login endpoint",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    // The class remains empty, Spring annotations handle everything!
}