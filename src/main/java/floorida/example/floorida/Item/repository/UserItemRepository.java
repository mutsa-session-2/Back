package floorida.example.floorida.Item.repository;

import floorida.example.floorida.Item.entity.ItemType;
import floorida.example.floorida.Item.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    boolean existsByUserIdAndItem_ItemId(Long userId, Long itemId);

    Optional<UserItem> findByUserIdAndItem_ItemId(Long userId, Long itemId);

    List<UserItem> findAllByUserId(Long userId);

    @Query("""
        SELECT ui FROM UserItem ui
        WHERE ui.userId = :userId
        AND ui.item.type = :type
        AND ui.equipped = true
    """)
    List<UserItem> findEquippedItemsByUserIdAndType(
            @Param("userId") Long userId,
            @Param("type") ItemType type
    );
}
