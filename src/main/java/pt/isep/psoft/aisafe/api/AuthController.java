package pt.isep.psoft.aisafe.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isep.psoft.aisafe.application.AuthenticateUserUseCase;
import pt.isep.psoft.aisafe.application.DTO.LoginRequest;
import pt.isep.psoft.aisafe.application.DTO.TokenResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUser;

    public AuthController(AuthenticateUserUseCase authenticateUser) {
        this.authenticateUser = authenticateUser;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse token = authenticateUser.execute(request);
        return ResponseEntity.ok(token);
    }
}