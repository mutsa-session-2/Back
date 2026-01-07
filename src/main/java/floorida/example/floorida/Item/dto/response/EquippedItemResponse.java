package floorida.example.floorida.Item.dto.response;

import floorida.example.floorida.Item.entity.Item;
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

    // ✅ 추가
    private Integer offsetX;
    private Integer offsetY;
    private Integer width;
    private Integer height;

    public EquippedItemResponse(Item item) {
        this.itemId = item.getItemId();
        this.itemName = item.getName();
        this.itemType = item.getType();
        this.imageUrl = item.getImgUrl();

        this.offsetX = item.getOffsetX();
        this.offsetY = item.getOffsetY();
        this.width = item.getWidth();
        this.height = item.getHeight();
    }
}
