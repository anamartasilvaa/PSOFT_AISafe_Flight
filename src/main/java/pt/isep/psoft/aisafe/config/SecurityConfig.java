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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    auth.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();

                    if (h2ConsoleEnabled) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // WP1 & WP1B
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/models/**").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/models/top5").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/top5").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/*/compatible-routes").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/*/real-time-status").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/operational-hours").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/*/operational-hours").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models/instances").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/instances/*/status").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/**").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ATCC", "ROLE_ADMIN");

                    // WP2 & WP2B
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/image").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/details").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/airports/grouped").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/airports/import").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/airports").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/status").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/airports/*/certifications").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/airports/**").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ATCC", "ROLE_ADMIN");

                    // WP3 & WP3B (Adicionada correção para o path da US216)
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/statistics/busiest-airports").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/routes").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/statistics/total-distance").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/search/alternatives").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN"); // Corrigido aqui
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/involving/*").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/export/geojson").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/routes").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/**").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.POST, "/api/flights").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/flights/**").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");

                    // WP4 & WP4B - ORDEM ESTRITA PARA EVITAR 403
                    auth.requestMatchers(HttpMethod.POST, "/api/maintenance/records").hasAnyAuthority("ROLE_MAINTENANCE_TECH", "ROLE_MAINTENANCE_SUPERVISOR", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/records/ongoing").hasAnyAuthority("ROLE_MAINTENANCE_SUPERVISOR", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/statistics/turnaround").hasAnyAuthority("ROLE_MAINTENANCE_SUPERVISOR","ROLE_MAINTENANCE_TECH", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/records/search").hasAnyAuthority("ROLE_MAINTENANCE_TECH", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/parts/low-stock-alerts").hasAnyAuthority("ROLE_MAINTENANCE_SUPERVISOR", "ROLE_ADMIN");

                    // Rotas de leitura ATCC
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/statistics/costs").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/alerts").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/records/total-hours").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/records/aircraft/*/total-hours").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");

                    // Regra genérica para o resto do WP4
                    auth.requestMatchers("/api/maintenance/**").hasAnyAuthority("ROLE_MAINTENANCE_TECH", "ROLE_MAINTENANCE_SUPERVISOR", "ROLE_ADMIN");

                    // --- NOVAS ROTAS (US224, US227, US229) ---
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/search-features").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/scheduled-flights/route-utilization").hasAnyAuthority("ROLE_BACKOFFICE", "ROLE_ADMIN");
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/fuel-efficiency").hasAnyAuthority("ROLE_ATCC", "ROLE_ADMIN");

                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403);
                            response.getWriter().write("{\"error\": \"Access denied: you don't have enough permissions.\"}");
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

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