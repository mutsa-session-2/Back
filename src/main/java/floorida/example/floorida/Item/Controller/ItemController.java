package floorida.example.floorida.Item.controller;

import floorida.example.floorida.Item.dto.response.ItemResponse;
import floorida.example.floorida.Item.entity.ItemType;
import floorida.example.floorida.Item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 아이템 조회
    @GetMapping
    public List<ItemResponse> getItems(
            @RequestParam ItemType type,
            @AuthenticationPrincipal Long userId
    ) {
        return itemService.getItemsByType(type, userId);
    }

    // 아이템 구매
    @PostMapping("/{itemId}/purchase")
    public void purchaseItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Long userId
    ) {
        itemService.purchaseItem(userId, itemId);
    }

    //아이템 장착
    @PostMapping("/{itemId}/equip")
    public void equip(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Long userId
    ) {
        itemService.equipItem(userId, itemId);
    }

    // 아이템 해제
    @PostMapping("/{itemId}/unequip")
    public void unequip(
            @PathVariable Long itemId,
            @AuthenticationPrincipal Long userId
    ) {
        itemService.unequipItem(userId, itemId);
    }
}
