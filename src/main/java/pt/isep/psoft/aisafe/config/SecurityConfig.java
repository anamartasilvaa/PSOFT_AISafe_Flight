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
                    // 2. WP1 - AIRCRAFT MANAGEMENT (Roles: BACKOFFICE, ATCC, ADMIN)
                    // US101: Register Model -> Backoffice + Admin
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

                    // US102 & US105: Register Instance & Update Status -> ATCC + Admin
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models/instances").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/instances/*/status").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

                    // US103 & US104: View/Search Aircraft -> All roles
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());

                    // 3. WP2 - AIRPORT MANAGEMENT (Roles: BACKOFFICE, ATCC, ADMIN)
                    // US106 & US109: Register Airport & Update Status -> Backoffice + Admin
                    auth.requestMatchers(HttpMethod.POST, "/api/airports").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/status").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

                    // US106a: Add Certification -> Backoffice, ATCC + Admin
                    auth.requestMatchers(HttpMethod.POST, "/api/airports/*/certifications").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());

                    // US107 & US108: View/Search Airports -> All roles
                    auth.requestMatchers(HttpMethod.GET, "/api/airports/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());

                    // 4. WP3 - FLIGHT ROUTES (Roles: ATCC, ADMIN)
                    // US110: Create Route -> ATCC + Admin
                    auth.requestMatchers(HttpMethod.POST, "/api/routes").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

                    // US112: Update & Deactivate Route -> ATCC + Admin
                    auth.requestMatchers(HttpMethod.PATCH, "/api/routes/**").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

                    // US113 & US114: View/Search Routes -> ATCC + Admin
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/**").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

                    // 5. WP4 - MAINTENANCE RECORDS (Roles: MAINTENANCE_TECH, ADMIN)
                    auth.requestMatchers("/api/maintenance/**").hasAnyRole(Role.MAINTENANCE_TECH.name(), Role.ADMIN.name());
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setStatus(403); // SC_FORBIDDEN
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