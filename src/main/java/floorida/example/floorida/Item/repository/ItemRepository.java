package floorida.example.floorida.Item.repository;

import floorida.example.floorida.Item.entity.Item;
import floorida.example.floorida.Item.entity.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByType(ItemType type);
}
