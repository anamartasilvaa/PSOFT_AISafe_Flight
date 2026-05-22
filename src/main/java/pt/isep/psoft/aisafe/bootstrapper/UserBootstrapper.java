package pt.isep.psoft.aisafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.isep.psoft.aisafe.application.UserService;
import pt.isep.psoft.aisafe.domain.Role;
import java.util.Set;

@Component
@Order(1)
public class UserBootstrapper implements CommandLineRunner {

    private final UserService userService;

    public UserBootstrapper(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            if (!userService.existsByUsername("admin@aisafe.pt")) {

                userService.registerUser(
                        "admin@aisafe.pt",
                        "AdminPass123!",
                        Set.of(Role.ADMIN)
                );

                System.out.println("BOOTSTRAP: System Administrator created!");
            }

            if (!userService.existsByUsername("operator@aisafe.pt")) {
                userService.registerUser(
                        "operator@aisafe.pt",
                        "OperatorPass123!",
                        Set.of(Role.BACKOFFICE)
                );
                System.out.println("BOOTSTRAP: Backoffice Operator created!");
            }


            if (!userService.existsByUsername("atcc@aisafe.pt")) {
                userService.registerUser(
                        "atcc@aisafe.pt",
                        "AtccPass123!",
                        Set.of(Role.ATCC)
                );
                System.out.println("BOOTSTRAP: ATCC created!");
            }


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