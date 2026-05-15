package pt.isep.psoft.aisafe.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.aisafe.domain.Role;
import pt.isep.psoft.aisafe.domain.User;
import pt.isep.psoft.aisafe.repositories.UserRepository;

import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists!");
        }

        // Encriptar a password antes de guardar
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User newUser = new User(username, encodedPassword, roles);
        userRepository.save(newUser);
    }

    // ISTO RESOLVE O "Cannot resolve method"
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}