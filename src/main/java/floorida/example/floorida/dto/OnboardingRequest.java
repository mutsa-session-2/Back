package floorida.example.floorida.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 온보딩에서 받는 사용자 성향 정보 요청 DTO.
 *
 * 프론트에서는 아래 Enum 이름을 그대로 문자열로 보내주면 됩니다.
 *
 * planningTendency:
 * - PROCRASTINATES     // 할 일을 최대한 미룬다
 * - PLANS_ONLY         // 계획은 세우는데 실천하지 못한다
 * - PLANS_AND_EXECUTES // 꼼꼼하게 계획을 세우고 이행한다
 *
 * dailyStudyHours:
 * - HOURS_0_1
 * - HOURS_1_3
 * - HOURS_3_6
 * - HOURS_6_10
 * - HOURS_10_PLUS
 */
@Getter
@Setter
public class OnboardingRequest {

    private String planningTendency;

    private String dailyStudyHours;
}


