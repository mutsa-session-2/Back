package floorida.example.floorida.Item.controller;

import floorida.example.floorida.Item.UserDetails.CustomUserDetails;
import floorida.example.floorida.Item.dto.response.ItemResponse;
import floorida.example.floorida.Item.dto.response.MyItemResponse;
import floorida.example.floorida.Item.dto.response.TeamMemberCharacterResponse;
import floorida.example.floorida.Item.entity.ItemType;
import floorida.example.floorida.Item.service.ItemService;
import floorida.example.floorida.Item.service.TeamCharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Item API", description = "아이템 상점 및 장착 관련 API")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final TeamCharacterService teamCharacterService;

    @Operation(
            summary = "아이템 목록 조회",
            description = """
        아이템 타입별로 아이템 목록을 조회합니다.
        - 이미 구매한 아이템은 owned = true 로 반환됩니다.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "아이템 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public List<ItemResponse> getItems(
            @Parameter(
                    description = "아이템 타입",
                    example = "TOP"
            )
            @RequestParam ItemType type,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return itemService.getItemsByType(type, userDetails.getUserId());
    }



    @Operation(
            summary = "아이템 구매",
            description = "아이템을 구매합니다. (포인트 차감 + 아이템 보유)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구매 성공"),
            @ApiResponse(responseCode = "400", description = "포인트 부족"),
            @ApiResponse(responseCode = "404", description = "아이템 없음")
    })
    // 아이템 구매
    @PostMapping("/{itemId}/purchase")
    public void purchaseItem(
            @Parameter(description = "아이템 ID", example = "1")
            @PathVariable Long itemId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        itemService.purchaseItem(userDetails.getUserId(), itemId);
    }

    //아이템 장착
    @Operation(
            summary = "아이템 장착",
            description = "보유 중인 아이템을 장착합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장착 성공"),
            @ApiResponse(responseCode = "400", description = "보유하지 않은 아이템")
    })
    @PostMapping("/{itemId}/equip")
    public void equip(
            @Parameter(description = "아이템 ID", example = "1")
            @PathVariable Long itemId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        itemService.equipItem(userDetails.getUserId(), itemId);
    }

    @Operation(
            summary = "아이템 해제",
            description = "장착 중인 아이템을 해제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "해제 성공")
    })
    // 아이템 해제
    @PostMapping("/{itemId}/unequip")
    public void unequip(
            @Parameter(description = "아이템 ID", example = "1")
            @PathVariable Long itemId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        itemService.unequipItem(userDetails.getUserId(), itemId);
    }


    @Operation(
            summary = "내가 보유한 아이템 조회",
            description = "사용자가 보유 중인 모든 아이템을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "보유 아이템 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public List<MyItemResponse> getMyItems(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return itemService.getMyItems(userDetails.getUserId());
    }

    @Operation(
            summary = "내가 장착한 아이템 조회",
            description = "사용자가 현재 장착 중인 아이템만 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "장착 아이템 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my/equipped")
    public List<MyItemResponse> getMyEquippedItems(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return itemService.getEquippedItems(userDetails.getUserId());
    }

    @Operation(
            summary = "팀원 캐릭터 상태 조회",
            description = """
                    팀플레이스에서 사용하기 위한 API입니다.
                    특정 팀에 속한 모든 팀원의 캐릭터(장착 아이템) 상태를 조회합니다.
                    
                    ✔ 요청자는 반드시 해당 팀의 멤버여야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀원 캐릭터 상태 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "팀 소속 아님"),
            @ApiResponse(responseCode = "404", description = "팀 없음")
    })
    @GetMapping("/{teamId}/characters")
    public List<TeamMemberCharacterResponse> getTeamCharacters(
            @Parameter(
                    description = "조회할 팀 ID",
                    example = "1"
            )
            @PathVariable Long teamId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return teamCharacterService.getTeamMembersCharacter(
                teamId,
                userDetails.getUserId()
        );
    }

}
