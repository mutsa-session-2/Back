package floorida.example.floorida.Item.dto.request;

import floorida.example.floorida.Item.entity.ItemType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//관리자용(나중에 아이템 추가할 때 사용)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemCreateRequest {

    private String name;
    private ItemType type;
    private int price;
    private String imgUrl;
    private String description;
}