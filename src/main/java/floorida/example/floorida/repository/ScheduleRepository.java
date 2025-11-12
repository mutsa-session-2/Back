package floorida.example.floorida.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import floorida.example.floorida.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByCreatorUserId(Long creatorUserId);
    List<Schedule> findByTeamId(Long teamId);
    Optional<Schedule> findByScheduleIdAndCreatorUserId(Long id, Long creatorUserId);
    List<Schedule> findByCreatorUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long creatorUserId, LocalDate start, LocalDate end);
}
