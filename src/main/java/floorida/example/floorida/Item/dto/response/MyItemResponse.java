package floorida.example.floorida.Item.dto.response;

import floorida.example.floorida.Item.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyItemResponse {

    private Long itemId;
    private String name;
    private ItemType type;
    private String imgUrl;
    private boolean equipped;
}