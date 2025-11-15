package floorida.example.floorida.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import floorida.example.floorida.entity.FloorStatus;

@Repository
public interface FloorStatusRepository extends JpaRepository<FloorStatus, Long> {

    Optional<FloorStatus> findByFloor_FloorIdAndUser_UserId(Long floorId, Long userId);

    boolean existsByFloor_FloorIdAndUser_UserId(Long floorId, Long userId);
}


