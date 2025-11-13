package floorida.example.floorida.repository;

import floorida.example.floorida.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByUser_UserId(Long userId);
    boolean existsByUser_UserId(Long userId);
}
