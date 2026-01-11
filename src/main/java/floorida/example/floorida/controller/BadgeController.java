package floorida.example.floorida.controller;

import floorida.example.floorida.Item.UserDetails.CustomUserDetails;
import floorida.example.floorida.dto.MyBadgeResponse;
import floorida.example.floorida.dto.TeamMemberBadgeResponse;
import floorida.example.floorida.service.BadgeService;
import floorida.example.floorida.team.entity.TeamMember;
import floorida.example.floorida.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Badge API", description = "뱃지 관련 API (조회/장착/해제 및 팀원 뱃지 조회)")
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final TeamService teamService;

    @Operation(
            summary = "내 보유 뱃지 조회",
            description = "로그인한 사용자가 보유한 모든 뱃지를 조회합니다."
    )
    @GetMapping("/my")
    public List<MyBadgeResponse> getMyBadges() {
        return badgeService.getMyBadges();
    }

    @Operation(
            summary = "내 장착 뱃지 조회",
            description = "로그인한 사용자가 현재 장착 중인 뱃지를 조회합니다."
    )
    @GetMapping("/my/equipped")
    public List<MyBadgeResponse> getMyEquippedBadges() {
        return badgeService.getMyEquippedBadges();
    }

    @Operation(
            summary = "뱃지 장착",
            description = "특정 뱃지를 장착합니다. (기존 장착 뱃지는 해제됨)"
    )
    @GetMapping("/{badgeId}/equip")
    public void equipBadge(@PathVariable Long badgeId) {
        badgeService.equipMyBadge(badgeId);
    }

    @Operation(
            summary = "뱃지 해제",
            description = "장착 중인 특정 뱃지를 해제합니다."
    )
    @GetMapping("/{badgeId}/unequip")
    public void unequipBadge(@PathVariable Long badgeId) {
        badgeService.unequipMyBadge(badgeId);
    }

    @Operation(
            summary = "팀원 뱃지 상태 조회",
            description = """
                    팀플레이스 - 팀원 목록에서 사용.
                    특정 팀에 속한 모든 팀원의 장착 뱃지 정보를 조회합니다.
                    
                    ✔ 요청자는 반드시 해당 팀의 멤버여야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀원 뱃지 조회 성공"),
            @ApiResponse(responseCode = "403", description = "팀 소속 아님"),
            @ApiResponse(responseCode = "404", description = "팀 없음")
    })
    @GetMapping("/team/{teamId}/members")
    public List<TeamMemberBadgeResponse> getTeamMemberBadges(
            @Parameter(description = "팀 ID", example = "1")
            @PathVariable Long teamId,
            
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 1. 요청자가 팀 멤버인지 검증
        teamService.getMember(teamId, userDetails.getUserId());

        // 2. 팀의 모든 멤버 조회
        List<TeamMember> members = teamService.getMembers(teamId);

        // 3. 각 멤버별 장착 뱃지 조회 및 DTO 변환
        return members.stream()
                .map(member -> {
                    List<MyBadgeResponse> badges = badgeService.getEquippedBadgesByUserId(member.getUser().getUserId());
                    return TeamMemberBadgeResponse.of(
                            member.getUser().getUserId(),
                            member.getUser().getUsername(),
                            badges
                    );
                })
                .toList();
    }
}
