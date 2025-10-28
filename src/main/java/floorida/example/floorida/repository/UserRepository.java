package floorida.example.floorida.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import floorida.example.floorida.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
}
