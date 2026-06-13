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
// WP1 & WP1B

// US201 & US202
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/models/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

// US204
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/models/top5").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

// US203, US205, US206
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/*/compatible-routes").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/*/real-time-status").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/operational-hours").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/instances/*/operational-hours").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

//(WP1)
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.POST, "/api/aircraft-models/instances").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/aircraft-models/instances/*/status").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/aircraft-models/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());


// WP2 & WP2B - AIRPORT MANAGEMENT

// US207
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/image").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

// US208
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/details").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

// US211
                    auth.requestMatchers(HttpMethod.GET, "/api/airports/grouped").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

//(WP2)
                    auth.requestMatchers(HttpMethod.POST, "/api/airports").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/airports/*/status").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.POST, "/api/airports/*/certifications").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/airports/**").hasAnyRole(Role.BACKOFFICE.name(), Role.ATCC.name(), Role.ADMIN.name());


// WP3 & WP2B - FLIGHT ROUTES & STATISTICS

// US210
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/statistics/busiest-airports").hasAnyRole(Role.BACKOFFICE.name(), Role.ADMIN.name());

// US209
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/involving/*").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

// (WP3)
                    auth.requestMatchers(HttpMethod.POST, "/api/routes").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.PATCH, "/api/routes/**").hasAnyRole(Role.ATCC.name(), Role.BACKOFFICE.name(), Role.ADMIN.name());
                    auth.requestMatchers(HttpMethod.GET, "/api/routes/**").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());

// WP4 - MAINTENANCE RECORDS
                    auth.requestMatchers(HttpMethod.GET, "/api/maintenance/records/total-hours").hasAnyRole(Role.ATCC.name(), Role.ADMIN.name());
                    auth.requestMatchers("/api/maintenance/**").hasAnyRole(Role.MAINTENANCE_TECH.name(), Role.ADMIN.name());

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