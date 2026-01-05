package floorida.example.floorida.Item.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TeamMemberCharacterResponse {

    private Long userId;
    private String username;

    // 캐릭터 외형용
    private List<EquippedItemResponse> equippedItems;
}