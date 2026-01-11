package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TeamResponse {

    @Schema(description = "팀 ID", example = "10")
    private Long teamId;

    @Schema(description = "팀 이름", example = "Floorida 팀플")
    private String name;

    @Schema(description = "팀 레벨 (진행도 또는 팀 등급 등)", example = "3")
    private Integer level;

    @Schema(description = "프로젝트 시작일", example = "2026-01-01")
    private LocalDate startDate;

    @Schema(description = "프로젝트 종료일", example = "2026-02-01")
    private LocalDate endDate;

    @Schema(description = "팀 생성 시각 (UTC ISO8601)", example = "2026-01-01T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "현재 사용자의 팀 내 역할", example = "owner", allowableValues = {"owner", "memeber"})
    private String myRole;

    @Schema(description = "팀 입장에 사용하는 초대 코드", example = "ABCD1234")
    private String joinCode;
}

