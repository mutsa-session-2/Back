package floorida.example.floorida.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import floorida.example.floorida.entity.UserBadge;
import floorida.example.floorida.entity.UserBadgeId;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, UserBadgeId> {

    boolean existsById_UserIdAndId_BadgeId(Long userId, Long badgeId);

    void deleteAllById_UserId(Long userId);

    @Query("""
        select ub from UserBadge ub
        join fetch ub.badge b
        where ub.user.userId = :userId
        order by ub.earnedAt asc
        """)
    List<UserBadge> findAllWithBadgeByUserId(@Param("userId") Long userId);

    @Query("""
        select ub from UserBadge ub
        join fetch ub.badge b
        where ub.user.userId = :userId and ub.equipped = true
        order by ub.earnedAt asc
        """)
    List<UserBadge> findEquippedBadgesByUserId(@Param("userId") Long userId);

    Optional<UserBadge> findById_UserIdAndId_BadgeId(Long userId, Long badgeId);
}
