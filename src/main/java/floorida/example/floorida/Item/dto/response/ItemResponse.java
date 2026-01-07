package floorida.example.floorida.Item.dto.response;

import floorida.example.floorida.Item.entity.Item;
import floorida.example.floorida.Item.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemResponse {

    private Long itemId;
    private String name;
    private ItemType type;
    private int price;
    private String imgUrl;
    private String description;
    private boolean owned;

    // ✅ 추가
    private Integer offsetX;
    private Integer offsetY;
    private Integer width;
    private Integer height;

    public ItemResponse(Item item, boolean owned) {
        this.itemId = item.getItemId();
        this.name = item.getName();
        this.type = item.getType();
        this.price = item.getPrice();
        this.imgUrl = item.getImgUrl();
        this.description = item.getDescription();
        this.owned = owned;

        this.offsetX = item.getOffsetX();
        this.offsetY = item.getOffsetY();
        this.width = item.getWidth();
        this.height = item.getHeight();
    }

}