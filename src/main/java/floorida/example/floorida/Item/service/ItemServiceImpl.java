package floorida.example.floorida.Item.service;

import floorida.example.floorida.Item.dto.response.ItemResponse;
import floorida.example.floorida.Item.entity.Item;
import floorida.example.floorida.Item.entity.ItemType;
import floorida.example.floorida.Item.entity.UserItem;
import floorida.example.floorida.Item.repository.ItemRepository;
import floorida.example.floorida.Item.repository.UserItemRepository;
import floorida.example.floorida.jhh.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final UserService userService;

    /**
     * 상점 아이템 조회
     * - type별 아이템 조회
     * - 유저가 이미 구매한 아이템 여부 포함
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByType(ItemType type, Long userId) {

        List<Item> items = itemRepository.findByType(type);
        List<UserItem> ownedItems = userItemRepository.findAllByUserId(userId);

        Set<Long> ownedItemIds = ownedItems.stream()
                .map(ui -> ui.getItem().getItemId())
                .collect(Collectors.toSet());

        return items.stream()
                .map(item -> new ItemResponse(
                        item.getItemId(),
                        item.getName(),
                        item.getType(),
                        item.getPrice(),
                        item.getImgUrl(),
                        item.getDescription(),
                        ownedItemIds.contains(item.getItemId())
                ))
                .toList();
    }

    /**
     * 아이템 구매
     */
    @Override
    public void purchaseItem(Long userId, Long itemId) {

        // 1️⃣ 아이템 존재 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 없음"));

        // 2️⃣ 이미 구매했는지 체크
        if (userItemRepository.existsByUserIdAndItem_ItemId(userId, itemId)) {
            throw new IllegalStateException("이미 구매한 아이템");
        }

        // 3️⃣ 코인 차감
        userService.deductCoin(userId, item.getPrice());

        // 4️⃣ UserItem 저장
        UserItem userItem = new UserItem(userId, item);
        userItemRepository.save(userItem);
    }

    /**
     * 아이템 장착
     */
    @Override
    public void equipItem(Long userId, Long itemId) {

        UserItem userItem = userItemRepository
                .findByUserIdAndItem_ItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 아이템"));

        // 같은 타입 아이템 전부 해제
        List<UserItem> equippedItems =
                userItemRepository.findEquippedItemsByUserIdAndType(
                        userId, userItem.getItem().getType());

        equippedItems.forEach(UserItem::unequip);

        // 장착
        userItem.equip();
    }

    /**
     * 아이템 해제
     */
    @Override
    public void unequipItem(Long userId, Long itemId) {

        UserItem userItem = userItemRepository
                .findByUserIdAndItem_ItemId(userId, itemId)
                .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 아이템"));

        userItem.unequip();
    }
}
