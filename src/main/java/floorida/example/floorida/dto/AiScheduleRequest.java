package floorida.example.floorida.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "AI 기반 일정 자동 생성 요청")
public class AiScheduleRequest {
    
    @NotBlank
    @Schema(
        description = """
            달성하고자 하는 목표를 자연어로 입력하세요.
            
            **작성 팁:**
            - 구체적일수록 더 정확한 계획이 생성됩니다
            - 목표의 난이도를 명시하면 더 적절한 단계 분배가 가능합니다
            - 특정 학습 방식이나 접근법을 명시할 수 있습니다
            
            **좋은 예시:**
            - "토익 900점 달성하기"
            - "기초부터 시작하는 파이썬 프로그래밍"
            - "매일 1시간씩 운동하는 루틴 만들기"
            - "팀 프로젝트 MVP 개발 완료"
            
            **나쁜 예시:**
            - "공부" (너무 추상적)
            - "운동" (목표가 불명확)
            """,
        example = "한 주 만에 자료구조 기본기 다지기",
        required = true,
        minLength = 1,
        maxLength = 500
    )
    private String goal;

    @NotNull
    @Schema(
        description = "시작일 (YYYY-MM-DD 형식)",
        example = "2025-10-24",
        required = true,
        type = "string",
        format = "date"
    )
    private LocalDate startDate;

    @NotNull
    @Schema(
        description = "종료일 (YYYY-MM-DD 형식, startDate 이후여야 함)",
        example = "2025-10-31",
        required = true,
        type = "string",
        format = "date"
    )
    private LocalDate endDate;

    @Schema(
        description = "팀 ID (팀 일정인 경우만 입력, 개인 일정은 null)",
        example = "null",
        required = false
    )
    private Long teamId;
    
    @Schema(
        description = "일정 색상 (HEX 코드, 미입력 시 자동 배정)",
        example = "#1E90FF",
        required = false,
        pattern = "^#[0-9A-Fa-f]{6}$"
    )
    private String color;
}
