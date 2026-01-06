package floorida.example.floorida.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyBadgeSummaryResponse {
    private List<MyBadgeResponse> badges;

    /**
     * 연속 출석 일수(일일 접속 보상 기준)
     */
    private int attendStreak;

    /**
     * attendStreak가 계산된 기준일(서버 로컬 날짜)
     */
    private LocalDate asOfDate;
}
