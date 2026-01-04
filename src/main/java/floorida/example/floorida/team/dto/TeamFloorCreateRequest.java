package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeamFloorCreateRequest {

    @Schema(description = "할 일 제목", example = "API 명세 작성")
    private String title;

    @Schema(description = "마감일(선택). 팀 프로젝트 기간(startDate~endDate) 범위 내만 허용", example = "2026-01-10", nullable = true)
    private LocalDate dueDate;

    @Schema(description = "배정자 userId 목록(복수). 비워도 됨(미정)", example = "[1,2]", nullable = true)
    private List<Long> assigneeUserIds;
}
