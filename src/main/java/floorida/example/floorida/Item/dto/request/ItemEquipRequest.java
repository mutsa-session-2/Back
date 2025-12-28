package floorida.example.floorida.Item.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 아이템 착용/해제
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemEquipRequest {

    private Long itemId;
    private boolean equipped;
}