package pt.isep.psoft.aisafe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pt.isep.psoft.aisafe.domain.Role;
import pt.isep.psoft.aisafe.infrastructure.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desativar CSRF (Justificação: Usamos tokens no header, não cookies)
                .csrf(csrf -> csrf.disable())

                // 2. Sessões Stateless (Sem estado no servidor)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Regras de Autorização
                .authorizeHttpRequests(auth -> {
                    // Endpoints Públicos
                    auth.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();

                    // Permitir H2 Console se ativada
                    if (h2ConsoleEnabled) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // Regras do Domínio (Ajusta as rotas conforme a tua API)
                    // Exemplo: GET aviões é público, POST precisa de ser ADMIN ou BACKOFFICE
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models/**").hasAnyRole(Role.ADMIN.name(), Role.BACKOFFICE.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/**").hasAnyRole(Role.ADMIN.name(), Role.BACKOFFICE.name());

                    auth.requestMatchers(HttpMethod.GET, "/api/airports/**").permitAll();
                    auth.requestMatchers(HttpMethod.POST, "/api/airports/**").hasAnyRole(Role.ADMIN.name()); // Só admin cria aeroportos? Ajusta se necessário!

                    // Qualquer outro pedido tem de estar pelo menos autenticado
                    auth.anyRequest().authenticated();
                })

                // 4. Injetar o teu Filtro ANTES do filtro normal do Spring
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Configuração extra para a consola H2 funcionar (usa frames)
        if (h2ConsoleEnabled) {
            http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        } else {
            http.headers(h -> h.frameOptions(f -> f.deny()));
        }

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}