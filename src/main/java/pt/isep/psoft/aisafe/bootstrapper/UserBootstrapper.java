package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.UserService;
import pt.isep.psoft.aisafe.domain.Role;
import java.util.Set;

@Component
@Order(1) // Garante que corre antes dos outros bootstrappers
public class UserBootstrapper implements CommandLineRunner {

    private final UserService userService;

    public UserBootstrapper(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            // 1. Verificar se o Admin já existe para não criar duplicados
            if (!userService.existsByUsername("admin@aisafe.pt")) {

                // AQUI É ONDE COLOCAS A LINHA:
                userService.registerUser(
                        "admin@aisafe.pt",
                        "AdminPass123!",
                        Set.of(Role.ADMIN)
                );

                System.out.println("BOOTSTRAP: System Administrator created!");
            }

            // 2. Podes fazer o mesmo para o Backoffice Operator
            if (!userService.existsByUsername("operator@aisafe.pt")) {
                userService.registerUser(
                        "operator@aisafe.pt",
                        "OperatorPass123!",
                        Set.of(Role.BACKOFFICE)
                );
                System.out.println("BOOTSTRAP: Backoffice Operator created!");
            }

            // 3. Criar o Colaborador da Empresa de Transportes (ATCC)
            if (!userService.existsByUsername("atcc@aisafe.pt")) {
                userService.registerUser(
                        "atcc@aisafe.pt",
                        "AtccPass123!",
                        Set.of(Role.ATCC)
                );
                System.out.println("BOOTSTRAP: ATCC created!");
            }

            // 4. Criar o Técnico de Manutenção (MAINTENANCE_TECH)
            if (!userService.existsByUsername("tech@aisafe.pt")) {
                userService.registerUser(
                        "tech@aisafe.pt",
                        "TechPass123!",
                        Set.of(Role.MAINTENANCE_TECH)
                );
                System.out.println("BOOTSTRAP: Maintenance Technician created!");
            }

        } catch (Exception e) {
            System.out.println("Error bootstrapping users: " + e.getMessage());
        }
    }
}