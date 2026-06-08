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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {

                    auth.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll();
                    auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();

                    if (h2ConsoleEnabled) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    // 2. WP1 - AIRCRAFT MANAGEMENT
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models/instances").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/instances/*/status").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());

                    // 3. WP2 - AIRPORT MANAGEMENT
                    auth.requestMatchers(HttpMethod.POST, "/api/airports").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/status").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.POST, "/api/airports/*/certifications").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/airports/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());

                    // 9. WP#2B/3 - STATISTICS (US210)
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/statistics/busiest-airports").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    // 4. WP3 - FLIGHT ROUTES
                    auth.requestMatchers(HttpMethod.POST, "/api/routes").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/routes/**").hasAnyRole(Role.ATCC.name(), Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/**").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

                    // 5. WP4 - MAINTENANCE RECORDS
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/records/total-hours").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers("/api/maintenance/**").hasAnyRole(Role.MAINTENANCE_TECH.name(), Role.ADMIN.name());

                    // 6. WP#1B - ENHANCED AIRCRAFT FEATURES
                    // US201 & US202 - Backoffice atualiza as especificações e imagem do Modelo
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/models/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

                    // 7. WP#2B - ENHANCED AIRPORT FEATURES (US207)
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/image").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

                    // 8. WP#2B - ENHANCED AIRPORT FEATURES (US208)
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/details").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

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