package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "팀 할 일 완료(토글 ON) 응답")
public class TeamFloorCompleteResponse {

    @Schema(description = "이미 완료된 상태였는지 여부(이미 완료면 true)", example = "false")
    private boolean alreadyCompleted;

    @Schema(description = "이번 요청으로 레벨이 증가했는지 여부", example = "true")
    private boolean levelUp;

    @Schema(description = "현재 팀 레벨(엘리베이터 층수)", example = "3")
    private Integer teamLevel;

    @Schema(
            description = "이번 완료로 지급된 코인 (마감 지났으면 0, 정상이면 10)",
            example = "10"
    )
    private int coinsAwarded;

    @Schema(
            description = "마감일을 지나 완료했는지 여부 (지각이면 true)",
            example = "false"
    )
    private boolean late;
}
