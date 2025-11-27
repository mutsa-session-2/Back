package floorida.example.floorida.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MonthlyScheduleResponse {
    private Long scheduleId;
    private String title;
    private String color;
    private LocalDate startDate;
    private LocalDate endDate;
}
