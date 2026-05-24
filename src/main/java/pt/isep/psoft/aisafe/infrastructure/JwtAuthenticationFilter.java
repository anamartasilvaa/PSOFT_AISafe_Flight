package pt.isep.psoft.aisafe.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pt.isep.psoft.aisafe.application.JwtService;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("\n--- A INICIAR PEDIDO PARA: " + request.getRequestURI() + " ---");

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("DEBUG 1: Cabeçalho Authorization ausente ou não começa com Bearer.");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("DEBUG 2: Token encontrado no cabeçalho.");

        if (jwtService.isTokenValid(token)) {
            System.out.println("DEBUG 3: O Token é VÁLIDO!");

            String subject = jwtService.extractSubject(token);
            List<String> roles = jwtService.extractRoles(token);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> {
                        String cleanRole = role.replace("[", "").replace("]", "").replace("\"", "").trim();
                        return new SimpleGrantedAuthority("ROLE_" + cleanRole);
                    })
                    .toList();

            System.out.println("DEBUG 4: Roles injetadas no Spring: " + authorities);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    subject, null, authorities
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            System.out.println("DEBUG ERRO: O Token foi considerado INVÁLIDO pelo jwtService!");
        }

        filterChain.doFilter(request, response);
    }
}