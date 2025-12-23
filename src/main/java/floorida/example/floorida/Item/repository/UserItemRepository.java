package floorida.example.floorida.Item.repository;

import floorida.example.floorida.Item.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    Optional<UserItem> findByUserIdAndItem_ItemId(Long userId, Long itemId);

    List<UserItem> findAllByUserId(Long userId);
}
