package pt.isep.psoft.aisafe.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.isep.psoft.aisafe.application.DTO.LoginRequest;
import pt.isep.psoft.aisafe.application.DTO.TokenResponse;
import pt.isep.psoft.aisafe.domain.exceptions.InvalidCredentialsException;
import pt.isep.psoft.aisafe.domain.User;
import pt.isep.psoft.aisafe.repositories.UserRepository;

@Service
public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public TokenResponse execute(LoginRequest request) {
        //Procurar o utilizador
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Gerar e devolver o Token
        return new TokenResponse(jwtService.generateToken(user));
    }
}