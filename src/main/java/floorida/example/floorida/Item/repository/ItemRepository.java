package floorida.example.floorida.Item.repository;

import floorida.example.floorida.Item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
