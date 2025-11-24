package floorida.example.floorida.controller;

import floorida.example.floorida.dto.DailyCompletionResponse;
import floorida.example.floorida.dto.FloorStatusResponse;
import floorida.example.floorida.service.FloorStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/floors")
@RequiredArgsConstructor
@Tag(name = "완료 상태 & 캘린더", description = "날짜별 Floor 완료 상태 조회 및 캘린더 통계 API. 프론트엔드 캘린더 UI에서 완료율 표시에 사용됩니다.")
@SecurityRequirement(name = "Bearer Authentication")
public class FloorStatusController {
    
    private final FloorStatusService floorStatusService;

    @GetMapping("/status/date/{date}")
    @Operation(
        summary = "특정 날짜 Floor 완료 상태 조회",
        description = """
            특정 날짜에 예정된 모든 Floor와 완료 여부를 조회합니다.
            
            **응답 정보:**
            - 해당 날짜의 모든 Floor 목록
            - 각 Floor의 완료 여부 (`completed: true/false`)
            - 일정 제목, 색상 포함
            
            **사용 예시:**
            - 캘린더에서 특정 날짜 클릭 시
            - 오늘 할 일 상세 조회
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        [
                          {
                            "floorId": 1,
                            "scheduleId": 1,
                            "scheduleTitle": "토익 900점 달성",
                            "scheduleColor": "#2E8B57",
                            "floorTitle": "RC 문법 복습",
                            "scheduledDate": "2025-11-20",
                            "completed": true
                          },
                          {
                            "floorId": 2,
                            "scheduleId": 1,
                            "scheduleTitle": "토익 900점 달성",
                            "scheduleColor": "#2E8B57",
                            "floorTitle": "LC Part 1-2",
                            "scheduledDate": "2025-11-20",
                            "completed": false
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<FloorStatusResponse>> getStatusByDate(
            @Parameter(description = "조회 날짜 (YYYY-MM-DD)", required = true, example = "2025-11-20")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(floorStatusService.getStatusByDate(date));
    }

    @GetMapping("/calendar")
    @Operation(
        summary = "기간별 완료 통계 조회 (캘린더용)",
        description = """
            지정한 기간 내 날짜별 완료 통계를 조회합니다.
            
            **응답 정보:**
            - 날짜별 전체/완료 Floor 개수
            - 완료율 (0~100%)
            - 각 날짜의 Floor 목록 포함
            
            **사용 예시:**
            - 주간 캘린더 뷰: `start=2025-11-18&end=2025-11-24`
            - 월간 캘린더 뷰: `start=2025-11-01&end=2025-11-30`
            
            **UI 활용:**
            - 완료율에 따라 날짜 색상 표시 (초록/노랑/빨강)
            - 100% 완료 날짜에 배지 표시
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        [
                          {
                            "date": "2025-11-20",
                            "totalFloors": 5,
                            "completedFloors": 3,
                            "completionRate": 60,
                            "floors": [
                              {
                                "floorId": 1,
                                "scheduleId": 1,
                                "scheduleTitle": "토익 900점",
                                "scheduleColor": "#2E8B57",
                                "floorTitle": "RC 문법",
                                "scheduledDate": "2025-11-20",
                                "completed": true
                              }
                            ]
                          },
                          {
                            "date": "2025-11-21",
                            "totalFloors": 3,
                            "completedFloors": 3,
                            "completionRate": 100,
                            "floors": []
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<List<DailyCompletionResponse>> getCalendarData(
            @Parameter(description = "시작 날짜 (YYYY-MM-DD)", required = true, example = "2025-11-18")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "종료 날짜 (YYYY-MM-DD)", required = true, example = "2025-11-24")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("종료 날짜는 시작 날짜 이후여야 합니다");
        }
        
        return ResponseEntity.ok(floorStatusService.getCalendarData(start, end));
    }
}
