package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "팀 할 일 목록 조회 응답(팀 레벨 포함)")
public record TeamFloorListResponse(

        @Schema(description = "현재 팀 레벨(층수)", example = "3")
        Integer teamLevel,

        @Schema(description = "팀 할 일 목록")
        List<TeamFloorResponse> floors
) {}
