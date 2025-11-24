package floorida.example.floorida.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import floorida.example.floorida.entity.FloorStatus;

@Repository
public interface FloorStatusRepository extends JpaRepository<FloorStatus, Long> {

    Optional<FloorStatus> findByFloor_FloorIdAndUser_UserId(Long floorId, Long userId);

    boolean existsByFloor_FloorIdAndUser_UserId(Long floorId, Long userId);
    
    // 특정 사용자의 특정 날짜 Floor 상태 조회
    List<FloorStatus> findAllByUser_UserIdAndFloor_ScheduledDate(Long userId, LocalDate date);
    
    // 특정 사용자의 기간별 Floor 상태 조회
    List<FloorStatus> findAllByUser_UserIdAndFloor_ScheduledDateBetween(Long userId, LocalDate start, LocalDate end);
}


