package floorida.example.floorida.Item.dto.response;

import floorida.example.floorida.Item.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EquippedItemResponse {

    private Long itemId;
    private String itemName;
    private ItemType itemType;
    private String imageUrl;
}