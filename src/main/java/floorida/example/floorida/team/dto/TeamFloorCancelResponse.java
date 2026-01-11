package floorida.example.floorida.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "팀 할 일 완료 취소(토글 OFF) 응답")
public class TeamFloorCancelResponse {

    @Schema(description = "이미 미완료 상태였는지 여부(이미 미완료면 true)", example = "false")
    private boolean alreadyIncomplete;

    @Schema(description = "이번 요청으로 레벨이 감소했는지 여부", example = "true")
    private boolean levelDown;

    @Schema(description = "현재 팀 레벨(엘리베이터 층수)", example = "2")
    private Integer teamLevel;

    @Schema(
            description = "이번 취소로 회수된 코인 (마감일 내 정상 완료였다면 10, 아니면 0)",
            example = "10"
    )
    private int coinsDeducted;
}
