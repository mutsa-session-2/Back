package floorida.example.floorida.team.service;

import floorida.example.floorida.entity.User;
import floorida.example.floorida.repository.UserRepository;
import floorida.example.floorida.team.entity.Team;
import floorida.example.floorida.team.entity.TeamMember;
import floorida.example.floorida.team.repository.TeamFloorRepository;
import floorida.example.floorida.team.repository.TeamFloorStatusRepository;
import floorida.example.floorida.team.repository.TeamMemberRepository;
import floorida.example.floorida.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final TeamFloorRepository teamFloorRepository;
    private final TeamFloorStatusRepository teamFloorStatusRepository;
    private final PasswordEncoder passwordEncoder;

    //*기능*//
    // 캘린더
    // 진행도

    public Long createTeam(Long userId, String name, LocalDate startDate, LocalDate endDate) {
        return createTeam(userId, name, null, startDate, endDate);
    }

    // 내 팀 조회
    @Transactional(readOnly = true)
    public Team getTeamOrThrow(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("team not found"));
    }

    // 팀 멤버 목록
    @Transactional(readOnly = true)
    public List<TeamMember> getMembers(Long teamId) {
        return teamMemberRepository.findByTeam_Id(teamId);
    }

    // 권한 체크 - 멤버인지
    @Transactional(readOnly = true)
    public TeamMember getMember(Long teamId, Long userId) {
        return teamMemberRepository.findByTeam_IdAndUser_UserId(teamId, userId)
                .orElseThrow(() -> new IllegalStateException("not a team member"));
    }

    // 권한 체크 - admin/owner인지
    @Transactional(readOnly = true)
    public void validateAdmin(Long teamId, Long userId) {
        TeamMember tm = getMember(teamId, userId);
        if (!"owner".equals(tm.getRole()) && !"admin".equals(tm.getRole())) {
            throw new IllegalStateException("no permission");
        }
    }

    // 권한 체크 - owner인지 (팀 삭제/퇴출용)
    @Transactional(readOnly = true)
    public void validateOwner(Long teamId, Long userId) {
        TeamMember tm = getMember(teamId, userId);
        if (!"owner".equals(tm.getRole())) {
            throw new IllegalStateException("no permission");
        }
    }

    // 팀 생성 (프로젝트 기간 포함)
    public Long createTeam(Long userId,
                           String name,
                           String description,
                           LocalDate startDate,
                           LocalDate endDate) {

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must be <= endDate");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        String joinCode = generateJoinCode();

        Team team = new Team(name, description, startDate, endDate, joinCode);
        teamRepository.save(team);

        TeamMember owner = new TeamMember(team, user, "owner");
        teamMemberRepository.save(owner);

        return team.getId();
    }

    // 초대코드 생성
    private String generateJoinCode() {
        String code;
        do {
            code = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8);
        } while (teamRepository.existsByJoinCode(code));
        return code;
    }

    // 팀 참가
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

    // 팀 삭제 (팀장만 + 비밀번호 재확인)
    public void deleteTeamWithPassword(Long teamId, Long requesterUserId, String password) {
        // 팀 존재 확인 + 방장 검증
        getTeamOrThrow(teamId);
        validateOwner(teamId, requesterUserId);

        // 비밀번호 검증
        User user = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid password");
        }

        // 팀의 모든 할 일 배정 제거
        teamFloorStatusRepository.deleteByTeamFloor_Team_Id(teamId);

        // 팀의 모든 할 일 제거
        teamFloorRepository.deleteByTeam_Id(teamId);

        // FK 고려: 멤버 먼저 삭제 후 팀 삭제
        teamMemberRepository.deleteByTeam_Id(teamId);
        teamRepository.deleteById(teamId);
    }

    // 팀원 퇴출 (팀장만)
    public void kickMember(Long teamId, Long requesterUserId, Long targetUserId) {
        getTeamOrThrow(teamId);
        validateOwner(teamId, requesterUserId);

        if (requesterUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("owner cannot kick self");
        }

        TeamMember target = getMember(teamId, targetUserId);

        if ("owner".equals(target.getRole())) {
            throw new IllegalArgumentException("cannot kick owner");
        }

        // 퇴출 시 배정된 할 일 제거 → 미정 가능
        teamFloorStatusRepository
                .deleteByIdUserIdAndTeamFloor_Team_Id(targetUserId, teamId);

        teamMemberRepository.delete(target);
    }

    // 팀 탈퇴 (멤버만) - 팀장은 불가
    public void leaveTeam(Long teamId, Long userId) {
        getTeamOrThrow(teamId);

        TeamMember me = getMember(teamId, userId);

        if ("owner".equals(me.getRole())) {
            throw new IllegalArgumentException("owner cannot leave; delete team instead");
        }

        // 탈퇴 시 배정된 할 일 맵핑 제거 (할 일은 미정 상태로 유지)
        teamFloorStatusRepository
                .deleteByIdUserIdAndTeamFloor_Team_Id(userId, teamId);

        teamMemberRepository.delete(me);
    }
}
