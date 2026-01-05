package floorida.example.floorida.Item.service;

import floorida.example.floorida.Exception.Item.TeamAccessDeniedException;
import floorida.example.floorida.Exception.Item.TeamNotFoundException;
import floorida.example.floorida.Item.dto.response.EquippedItemResponse;
import floorida.example.floorida.Item.dto.response.TeamMemberCharacterResponse;
import floorida.example.floorida.Item.entity.Item;
import floorida.example.floorida.Item.entity.UserItem;
import floorida.example.floorida.Item.repository.TeamMemberCharacterRepository;
import floorida.example.floorida.Item.repository.UserItemRepository;
import floorida.example.floorida.team.entity.TeamMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamCharacterServiceImpl implements TeamCharacterService {

    private final TeamMemberCharacterRepository teamMemberCharacterRepository;
    private final UserItemRepository userItemRepository;

    @Override
    public List<TeamMemberCharacterResponse> getTeamMembersCharacter(
            Long teamId,
            Long requesterUserId
    ) {
        // 1️⃣ 요청자 팀 소속 검증
        teamMemberCharacterRepository.findByTeam_IdAndUser_UserId(teamId, requesterUserId)
                .orElseThrow(() -> new TeamAccessDeniedException("팀에 속하지 않은 사용자입니다."));

        // 요청 팀 소속 검증
        teamMemberCharacterRepository
                .findByTeam_IdAndUser_UserId(teamId, requesterUserId)
                .orElseThrow(() -> new TeamNotFoundException("존재하지 않는 팀입니다."));

        // 2️⃣ 팀 멤버 조회
        List<TeamMember> members = teamMemberCharacterRepository.findAllByTeam_Id(teamId);

        // 3️⃣ 팀원 캐릭터 상태 구성
        return members.stream()
                .map(member -> {
                    Long userId = member.getUser().getUserId();
                    String nickname = member.getUser().getUsername();

                    // 4️⃣ 장착 아이템 조회 (네 Repository 그대로 사용)
                    List<EquippedItemResponse> equippedItems =
                            userItemRepository.findAllByUserIdAndEquippedTrue(userId)
                                    .stream()
                                    .map(this::toEquippedItemResponse)
                                    .toList();

                    return new TeamMemberCharacterResponse(
                            userId,
                            nickname,
                            equippedItems
                    );
                })
                .toList();
    }

    private EquippedItemResponse toEquippedItemResponse(UserItem userItem) {
        Item item = userItem.getItem();

        return new EquippedItemResponse(
                item.getItemId(),   // ✅ 네 Item PK
                item.getName(),
                item.getType(),
                item.getImgUrl()
        );
    }
}