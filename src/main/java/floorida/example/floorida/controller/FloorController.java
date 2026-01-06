package floorida.example.floorida.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import floorida.example.floorida.dto.FloorResponse;
import floorida.example.floorida.dto.FloorUpdateRequest;
import floorida.example.floorida.service.FloorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/floors")
@Tag(name = "오늘 할 일", description = "날짜별 Floor 조회 API. 프론트엔드에서 오늘 할 일 화면 구성에 사용합니다.")
@SecurityRequirement(name = "Bearer Authentication")
public class FloorController {

    private final FloorService floorService;

    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }

    @GetMapping("/today")
    @Operation(
        summary = "오늘 할 일 조회",
        description = """
            로그인한 사용자의 오늘 날짜(scheduledDate)에 해당하는 모든 Floor를 조회합니다.
            
            **사용 사례:**
            - 메인 화면에서 "오늘 할 일" 목록 표시
            - 각 Floor는 소속 일정 정보(제목, 색상)를 포함하여 반환
            
            **반환 정보:**
            - Floor 기본 정보 (ID, 제목, 날짜)
            - 소속 일정 정보 (ID, 제목, 색상) - UI 표시용
            
            **권한:**
            - JWT 토큰 필수
            - 본인이 생성한 Floor만 조회
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = FloorResponse.class)),
                examples = @ExampleObject(
                    name = "오늘 할 일 목록",
                    value = """
                        [
                          {
                            "floorId": 1,
                            "scheduleId": 1,
                            "scheduleTitle": "토익 900점 달성",
                            "scheduleColor": "#FF6B6B",
                            "floorTitle": "RC 파트 총정리",
                            "scheduledDate": "2025-11-13"
                          },
                          {
                            "floorId": 5,
                            "scheduleId": 2,
                            "scheduleTitle": "운동 루틴 만들기",
                            "scheduleColor": "#4ECDC4",
                            "floorTitle": "스쿼트 100개",
                            "scheduledDate": "2025-11-13"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<FloorResponse>> getTodayFloors() {
        return ResponseEntity.ok(floorService.getTodayFloors());
    }

    @GetMapping("/date/{date}")
    @Operation(
        summary = "특정 날짜 할 일 조회",
        description = """
            로그인한 사용자의 특정 날짜에 해당하는 모든 Floor를 조회합니다.
            
            **사용 사례:**
            - 캘린더 뷰에서 특정 날짜 클릭 시 해당 날짜 할 일 표시
            - 과거/미래 날짜의 계획 확인
            
            **날짜 형식:**
            - YYYY-MM-DD (예: 2025-11-20)
            
            **권한:**
            - JWT 토큰 필수
            - 본인이 생성한 Floor만 조회
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = FloorResponse.class)),
                examples = @ExampleObject(
                    name = "특정 날짜 할 일",
                    value = """
                        [
                          {
                            "floorId": 3,
                            "scheduleId": 1,
                            "scheduleTitle": "토익 900점 달성",
                            "scheduleColor": "#FF6B6B",
                            "floorTitle": "실전 모의고사 3회",
                            "scheduledDate": "2025-11-20"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 날짜 형식",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<List<FloorResponse>> getFloorsByDate(
        @Parameter(
            description = "조회할 날짜 (YYYY-MM-DD 형식)",
            required = true,
            example = "2025-11-20"
        )
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(floorService.getFloorsByDate(date));
    }

    @PostMapping("/{floorId}/complete")
    @Operation(
                summary = "Floor 완료 체크 (10코인 지급)",
        description = """
                        지정한 Floor를 완료 처리하고, 해당 사용자에게 **10코인**을 지급합니다.

                        - 코인 정책
                            - 퀘스트(층, Floor) 하나를 체크(완료)할 때마다 **10코인**
            - 중복 보호
              - 이미 완료된 Floor를 다시 완료하려 하면 400 에러가 발생합니다.
            - 권한
              - JWT 토큰 필수
              - 본인이 생성한 Floor만 완료 가능
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "완료 처리 성공 (10코인 지급됨)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "이미 완료된 Floor, 권한 없음 또는 기타 잘못된 요청",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<?> completeFloor(
        @Parameter(description = "완료 처리할 Floor ID", required = true, example = "1")
        @PathVariable Long floorId
    ) {
        try {
            floorService.completeFloor(floorId);
            return ResponseEntity.ok("Floor completed! +10 coins");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{floorId}")
    @Operation(
        summary = "Floor 수정 (제목/날짜)",
        description = """
            지정한 Floor(세부 일정)의 제목과/또는 날짜를 수정합니다.

            - 제목만 수정: `{ "title": "새 제목" }`
            - 날짜만 이동: `{ "scheduledDate": "2025-11-20" }`
            - 둘 다 수정: `{ "title": "새 제목", "scheduledDate": "2025-11-20" }`

            **제약:**
            - 날짜를 변경하는 경우, 해당 Floor의 일정 기간(startDate~endDate) 안에서만 이동 가능합니다
            - 같은 일정(schedule) 안에서 같은 날짜에 Floor는 1개만 허용됩니다

            **사용 사례:**
            - 캘린더/프로젝트 수정 화면에서 일차(할 일) 내용을 원하는 텍스트로 변경

            **권한:**
            - JWT 토큰 필수
            - 본인이 생성한 Floor만 수정 가능
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FloorResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "floorId": 1,
                          "scheduleId": 1,
                          "scheduleTitle": "토익 900점 달성",
                          "scheduleColor": "#FF6B6B",
                          "floorTitle": "RC 파트 총정리",
                          "scheduledDate": "2025-11-13"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 권한 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<FloorResponse> updateFloor(
        @Parameter(description = "수정할 Floor ID", required = true, example = "1")
        @PathVariable Long floorId,
        @Valid @RequestBody FloorUpdateRequest req
    ) {
        return ResponseEntity.ok(floorService.updateFloor(floorId, req));
    }

    @DeleteMapping("/{floorId}")
    @Operation(
        summary = "Floor 삭제",
        description = """
            지정한 Floor(세부 일정)를 영구 삭제합니다.

            - 연관된 완료 기록(FloorStatus)도 함께 정리되어 500 오류가 발생하지 않도록 처리합니다.
            - JWT 토큰 필수
            - 본인이 생성한 Floor만 삭제 가능
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공 (응답 본문 없음)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "Floor 없음")
    })
    public ResponseEntity<Void> deleteFloor(
        @Parameter(description = "삭제할 Floor ID", required = true, example = "1")
        @PathVariable Long floorId
    ) {
        floorService.deleteFloor(floorId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/schedule/{scheduleId}")
    @Operation(
        summary = "(호환용) 일정 삭제",
        description = """
            일정 ID(scheduleId)에 대해 **일정 자체를 삭제**합니다.
            (기존 클라이언트 호환을 위해 `/api/floors/schedule/{scheduleId}` 경로를 유지합니다)

            - 일정에 포함된 모든 Floor(세부 일정)도 함께 영구 제거됩니다.
            - 연관된 완료 기록(FloorStatus)도 함께 정리되어 500 오류가 발생하지 않도록 처리합니다.
            - JWT 토큰 필수
            - 본인이 생성한 일정만 삭제 가능
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공 (응답 본문 없음)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "Schedule 없음")
    })
    public ResponseEntity<Void> deleteFloorsBySchedule(
        @Parameter(description = "floors를 삭제할 일정 ID", required = true, example = "1")
        @PathVariable Long scheduleId
    ) {
        floorService.deleteFloorsBySchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
