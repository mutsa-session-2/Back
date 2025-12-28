package floorida.example.floorida.Item.dto.response;

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

}