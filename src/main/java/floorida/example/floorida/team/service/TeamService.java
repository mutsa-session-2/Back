package floorida.example.floorida.team.service;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;
import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.entity.TeamMember;
import floorida.example.floorida.team.repository.TeamMemberRepository;
import floorida.example.floorida.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    //*기능*//
    //캘린더
    //진행도

    //권한 체크 - 멤버인지, 어드민인지
    public TeamMember getMember(Long teamId, Long userId) {
        return teamMemberRepository.findByTeam_IdAndUser_UserId(teamId, userId)
                .orElseThrow(() -> new IllegalStateException("not a team member"));
    }

    public void validateAdmin(Long teamId, Long userId) {
        TeamMember tm = getMember(teamId, userId);
        if (!"owner".equals(tm.getRole()) && !"admin".equals(tm.getRole())) {
            throw new IllegalStateException("no permission");
        }
    }


    //팀 생성
    public Long createTeam(Long userId, String name, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        String joinCode = generateJoinCode();

        Team team = new Team(name, description, joinCode);
        teamRepository.save(team);

        TeamMember owner = new TeamMember(team, user, "owner");
        teamMemberRepository.save(owner);

        return team.getId();
    }

    //초대 코드 생성
    private String generateJoinCode() {
        String code;
        do {
            code = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8);
        } while (teamRepository.existsByJoinCode(code));
        return code;
    }

    //팀 초대
    public void joinTeam(Long userId, String joinCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Team team = teamRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new IllegalArgumentException("invalid join code"));

        if (teamMemberRepository.existsByTeam_IdAndUser_UserId(team.getId(), userId)) {
            throw new IllegalStateException("already joined this team");
        }


        TeamMember member = new TeamMember(team, user, "member");
        teamMemberRepository.save(member);
    }


}
