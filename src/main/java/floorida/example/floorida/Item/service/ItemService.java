package floorida.example.floorida.Item.service;

import floorida.example.floorida.Item.dto.response.ItemResponse;
import floorida.example.floorida.Item.entity.ItemType;

import java.util.List;

public interface ItemService {

    List<ItemResponse> getItemsByType(ItemType type, Long userId);

    void purchaseItem(Long userId, Long itemId);

    void equipItem(Long userId, Long itemId);

    void unequipItem(Long userId, Long itemId);
}