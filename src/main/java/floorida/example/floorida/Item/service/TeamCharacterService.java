package floorida.example.floorida.Item.service;

import floorida.example.floorida.Item.dto.response.TeamMemberCharacterResponse;

import java.util.List;

public interface TeamCharacterService {

    List<TeamMemberCharacterResponse> getTeamMembersCharacter(
            Long teamId,
            Long requesterUserId
    );
}