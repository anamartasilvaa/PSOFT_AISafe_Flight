package pt.isep.psoft.aisafe.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.aisafe.domain.User;

import java.util.Optional;

@Repository // O Spring precisa disto para injetar o bean!
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Este é o método que vai permitir ao Service saber se o user já lá está
    boolean existsByUsername(String username);
}