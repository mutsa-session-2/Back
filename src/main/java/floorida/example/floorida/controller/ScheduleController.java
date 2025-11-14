package floorida.example.floorida.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import floorida.example.floorida.dto.AiScheduleRequest;
import floorida.example.floorida.dto.ScheduleCreateRequest;
import floorida.example.floorida.dto.ScheduleResponse;
import floorida.example.floorida.dto.ScheduleUpdateRequest;
import floorida.example.floorida.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedules")
@Validated
@Tag(name = "일정 관리", description = "일정(Schedule)과 세부 계획(Floor) 생성, 조회 API. 개인 또는 팀 일정을 수동/AI로 생성할 수 있습니다.")
@SecurityRequirement(name = "Bearer Authentication")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    @Operation(
        summary = "수동으로 일정 생성",
        description = """
            사용자가 직접 일정 제목, 기간, 세부 계획(floors)을 입력하여 일정을 생성합니다.
            
            **특징:**
            - 세부 계획(floors)은 선택 사항이며, 생략 시 빈 일정만 생성됩니다
            - 팀 일정인 경우 `teamId`를 포함하면 팀원들과 공유됩니다
            - 개인 일정인 경우 `teamId`는 null로 설정합니다
            - `color`는 HEX 코드 형식(예: #FF5733)으로 입력 가능합니다
            
            **권한:**
            - JWT 토큰 필수
            - 로그인한 사용자만 자신의 일정 생성 가능
            
            **세부 계획(Floor) 설명:**
            - 각 층(floor)은 일정 내 하나의 작은 단계/할 일을 나타냅니다
            - `scheduledDate`를 지정하면 해당 날짜에 해당 층을 수행하도록 계획됩니다
            - `scheduledDate`를 생략하면 날짜 없이 순서만 지정됩니다
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "일정 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleResponse.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    value = """
                        {
                          "scheduleId": 1,
                          "title": "토익 900점 달성",
                          "startDate": "2025-10-24",
                          "endDate": "2025-10-31",
                          "color": "#2E8B57",
                          "teamId": null,
                          "floors": [
                            {
                              "floorId": 1,
                              "title": "RC 파트 총정리",
                              "scheduledDate": "2025-10-24"
                            },
                            {
                              "floorId": 2,
                              "title": "LC 파트 모의고사 1회",
                              "scheduledDate": "2025-10-25"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 - 유효성 검증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "날짜 범위 오류",
                    value = """
                        {
                          "error": "Invalid date range",
                          "message": "종료일이 시작일보다 이전일 수 없습니다"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 - JWT 토큰 없음 또는 만료",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Unauthorized",
                          "message": "유효한 인증 토큰이 필요합니다"
                        }
                        """
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = """
            수동 일정 생성 요청 본문
            
            **필수 필드:**
            - `title`: 일정 제목 (최소 1자)
            - `startDate`: 시작일 (YYYY-MM-DD 형식)
            - `endDate`: 종료일 (YYYY-MM-DD 형식, startDate 이후여야 함)
            
            **선택 필드:**
            - `color`: 일정 색상 (HEX 코드, 예: #FF5733)
            - `teamId`: 팀 ID (팀 일정인 경우만 입력)
            - `floors`: 세부 계획 배열 (각 floor는 title 필수, scheduledDate 선택)
            """,
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ScheduleCreateRequest.class),
            examples = {
                @ExampleObject(
                    name = "개인 일정 예시",
                    value = """
                        {
                          "title": "토익 900점 달성",
                          "startDate": "2025-10-24",
                          "endDate": "2025-10-31",
                          "color": "#2E8B57",
                          "teamId": null,
                          "floors": [
                            {
                              "title": "RC 파트 총정리",
                              "scheduledDate": "2025-10-24"
                            },
                            {
                              "title": "LC 파트 모의고사 1회",
                              "scheduledDate": "2025-10-25"
                            },
                            {
                              "title": "실전 모의고사 3회",
                              "scheduledDate": "2025-10-26"
                            }
                          ]
                        }
                        """
                ),
                @ExampleObject(
                    name = "팀 일정 예시",
                    value = """
                        {
                          "title": "팀 프로젝트 완성",
                          "startDate": "2025-11-01",
                          "endDate": "2025-11-30",
                          "color": "#1E90FF",
                          "teamId": 5,
                          "floors": [
                            {
                              "title": "기획안 작성",
                              "scheduledDate": "2025-11-01"
                            },
                            {
                              "title": "프론트엔드 개발",
                              "scheduledDate": "2025-11-10"
                            },
                            {
                              "title": "백엔드 API 개발",
                              "scheduledDate": "2025-11-15"
                            }
                          ]
                        }
                        """
                ),
                @ExampleObject(
                    name = "세부 계획 없는 빈 일정",
                    value = """
                        {
                          "title": "독서 습관 만들기",
                          "startDate": "2025-12-01",
                          "endDate": "2025-12-31",
                          "color": "#FF6347",
                          "teamId": null,
                          "floors": []
                        }
                        """
                )
            }
        )
    )
    public ResponseEntity<ScheduleResponse> createManual(@Valid @RequestBody ScheduleCreateRequest req) {
        return ResponseEntity.ok(scheduleService.createManual(req));
    }

    @PostMapping("/ai")
    @Operation(
        summary = "AI로 일정 자동 생성",
        description = """
            사용자가 목표(goal)와 기간을 입력하면 OpenAI가 자동으로 세부 계획(floors)을 생성합니다.
            
            **동작 방식:**
            1. 사용자가 자연어로 목표 입력 (예: "한 달 안에 파이썬 마스터하기")
            2. OpenAI Chat Completions API 호출하여 기간 내 단계별 계획 생성
            3. 생성된 계획을 일정과 세부 계획(floors)으로 자동 저장
            
            **AI 생성 규칙:**
            - 기간 내 날짜를 균등하게 분배하여 단계별 계획 생성
            - 목표의 난이도와 기간을 고려하여 적절한 단계 수 제안
            - 각 단계마다 구체적인 행동 계획 제시
            
            **Fallback 처리:**
            - OpenAI API 호출 실패 시 자동으로 기본 계획 생성
            - API 키가 없거나 네트워크 오류 시에도 서비스 중단 없음
            
            **권한:**
            - JWT 토큰 필수
            - 로그인한 사용자만 자신의 일정 생성 가능
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "AI 일정 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleResponse.class),
                examples = @ExampleObject(
                    name = "AI 생성 성공 예시",
                    value = """
                        {
                          "scheduleId": 2,
                          "title": "한 주 만에 자료구조 기본기 다지기",
                          "startDate": "2025-10-24",
                          "endDate": "2025-10-31",
                          "color": "#1E90FF",
                          "teamId": null,
                          "floors": [
                            {
                              "floorId": 3,
                              "title": "배열과 연결 리스트 개념 이해",
                              "scheduledDate": "2025-10-24"
                            },
                            {
                              "floorId": 4,
                              "title": "스택과 큐 구현 연습",
                              "scheduledDate": "2025-10-25"
                            },
                            {
                              "floorId": 5,
                              "title": "트리와 그래프 기초",
                              "scheduledDate": "2025-10-26"
                            },
                            {
                              "floorId": 6,
                              "title": "정렬 알고리즘 학습",
                              "scheduledDate": "2025-10-27"
                            },
                            {
                              "floorId": 7,
                              "title": "해시 테이블 원리 이해",
                              "scheduledDate": "2025-10-28"
                            },
                            {
                              "floorId": 8,
                              "title": "알고리즘 문제 풀이",
                              "scheduledDate": "2025-10-29"
                            },
                            {
                              "floorId": 9,
                              "title": "복습 및 정리",
                              "scheduledDate": "2025-10-30"
                            },
                            {
                              "floorId": 10,
                              "title": "모의 코딩 테스트",
                              "scheduledDate": "2025-10-31"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Invalid date range",
                          "message": "시작일과 종료일을 올바르게 입력해주세요"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "AI 생성 실패 (Fallback으로 기본 계획 생성됨)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "warning": "AI 생성에 실패했지만 기본 계획으로 생성되었습니다"
                        }
                        """
                )
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = """
            AI 일정 생성 요청 본문
            
            **필수 필드:**
            - `goal`: 달성하고자 하는 목표를 자연어로 입력 (예: "토익 900점 달성", "파이썬 기초 마스터")
            - `startDate`: 시작일 (YYYY-MM-DD 형식)
            - `endDate`: 종료일 (YYYY-MM-DD 형식)
            
            **선택 필드:**
            - `teamId`: 팀 ID (팀 일정인 경우만)
            - `color`: 일정 색상 (HEX 코드)
            
            **Goal 작성 팁:**
            - 구체적일수록 더 정확한 계획 생성 (예: "토익" 보다 "토익 900점 달성"이 좋음)
            - 목표의 난이도를 명시하면 더 적절한 단계 분배 (예: "기초부터 시작하는 파이썬")
            - 특정 학습 방식 명시 가능 (예: "매일 1시간씩 운동 루틴 만들기")
            """,
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AiScheduleRequest.class),
            examples = {
                @ExampleObject(
                    name = "학습 목표",
                    value = """
                        {
                          "goal": "한 달 안에 Spring Boot 웹 개발 기초 다지기",
                          "startDate": "2025-11-01",
                          "endDate": "2025-11-30",
                          "teamId": null,
                          "color": "#4CAF50"
                        }
                        """
                ),
                @ExampleObject(
                    name = "운동 목표",
                    value = """
                        {
                          "goal": "10kg 감량을 위한 매일 운동 루틴",
                          "startDate": "2025-12-01",
                          "endDate": "2026-02-28",
                          "teamId": null,
                          "color": "#FF5722"
                        }
                        """
                ),
                @ExampleObject(
                    name = "프로젝트 목표",
                    value = """
                        {
                          "goal": "팀 협업 프로젝트 MVP 개발 완료",
                          "startDate": "2025-11-15",
                          "endDate": "2025-12-15",
                          "teamId": 3,
                          "color": "#2196F3"
                        }
                        """
                ),
                @ExampleObject(
                    name = "자격증 준비",
                    value = """
                        {
                          "goal": "정보처리기사 필기 시험 준비",
                          "startDate": "2026-01-01",
                          "endDate": "2026-03-01",
                          "teamId": null,
                          "color": "#9C27B0"
                        }
                        """
                )
            }
        )
    )
    public ResponseEntity<ScheduleResponse> createWithAi(@Valid @RequestBody AiScheduleRequest req) {
        return ResponseEntity.ok(scheduleService.createWithAi(req));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "일정 단건 조회",
        description = """
            특정 일정의 상세 정보를 조회합니다. 일정에 포함된 모든 세부 계획(floors)도 함께 반환됩니다.
            
            **조회 가능 조건:**
            - 본인이 생성한 일정만 조회 가능
            - 팀 일정의 경우 해당 팀 멤버만 조회 가능 (추후 구현 예정)
            
            **반환 정보:**
            - 일정 기본 정보 (제목, 기간, 색상 등)
            - 세부 계획(floors) 전체 목록
            - 각 층의 완료 여부 (추후 구현 예정)
            
            **권한:**
            - JWT 토큰 필수
            - 로그인한 사용자가 생성한 일정만 조회 가능
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleResponse.class),
                examples = @ExampleObject(
                    name = "조회 성공 예시",
                    value = """
                        {
                          "scheduleId": 1,
                          "title": "토익 900점 달성",
                          "startDate": "2025-10-24",
                          "endDate": "2025-10-31",
                          "color": "#2E8B57",
                          "teamId": null,
                          "floors": [
                            {
                              "floorId": 1,
                              "title": "RC 파트 총정리",
                              "scheduledDate": "2025-10-24"
                            },
                            {
                              "floorId": 2,
                              "title": "LC 파트 모의고사 1회",
                              "scheduledDate": "2025-10-25"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Unauthorized",
                          "message": "로그인이 필요합니다"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "일정을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Schedule not found",
                          "message": "요청한 일정이 존재하지 않거나 접근 권한이 없습니다"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ScheduleResponse> get(
        @Parameter(
            description = "조회할 일정의 고유 ID",
            required = true,
            example = "1"
        )
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(scheduleService.getById(id));
    }

      @PatchMapping("/{id}")
      @Operation(
        summary = "일정 부분 수정",
        description = """
            일정의 제목, 기간, 색상, 목표 요약을 부분적으로 수정합니다.
            
            **특징:**
            - 제공된 필드만 변경되며 나머지는 유지됩니다
            - 원래 목표(originalGoal)는 수정할 수 없습니다 (AI 생성 시 입력한 원본 유지)
            - 날짜 범위 유효성 검사 적용 (startDate ≤ endDate)
            
            **권한:**
            - JWT 토큰 필수
            - 본인이 생성한 일정만 수정 가능
            
            **수정 가능 필드:**
            - `title`: 일정 표시 제목
            - `goalSummary`: 목표 요약/설명
            - `startDate`, `endDate`: 일정 기간
            - `color`: 색상
            """
      )
      @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "수정 성공", 
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScheduleResponse.class),
                examples = @ExampleObject(
                    name = "수정 성공 예시",
                    value = """
                        {
                          "scheduleId": 1,
                          "title": "자료구조 집중 학습 주간 (수정됨)",
                          "originalGoal": "한 주 만에 자료구조 기본기 다지기",
                          "goalSummary": "일주일 동안 핵심 자료구조만 빠르게 정리",
                          "startDate": "2025-11-16",
                          "endDate": "2025-11-23",
                          "color": "#FF6B6B",
                          "teamId": null,
                          "floors": [
                            {
                              "floorId": 1,
                              "title": "배열과 연결 리스트",
                              "scheduledDate": "2025-11-16"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "유효성 오류",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Invalid date range: end before start",
                          "message": "종료일이 시작일보다 이전일 수 없습니다"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 실패",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "일정 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Schedule not found",
                          "message": "요청한 일정이 존재하지 않거나 권한이 없습니다"
                        }
                        """
                )
            )
        )
      })
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = """
            일정 수정 요청 본문 (모든 필드 선택 사항)
            
            **제공하지 않은 필드는 변경되지 않습니다.**
            
            **예시 사용 사례:**
            - 제목만 변경: `{ "title": "새 제목" }`
            - 기간만 연장: `{ "endDate": "2025-12-31" }`
            - 색상 + 요약 변경: `{ "color": "#FF6B6B", "goalSummary": "새 설명" }`
            """,
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ScheduleUpdateRequest.class),
            examples = {
                @ExampleObject(
                    name = "제목만 변경",
                    value = """
                        {
                          "title": "자료구조 집중 학습 주간"
                        }
                        """
                ),
                @ExampleObject(
                    name = "기간 연장",
                    value = """
                        {
                          "endDate": "2025-12-31"
                        }
                        """
                ),
                @ExampleObject(
                    name = "전체 수정",
                    value = """
                        {
                          "title": "Spring Boot 심화 학습",
                          "startDate": "2025-11-20",
                          "endDate": "2025-12-20",
                          "goalSummary": "JPA, Security, Batch를 한 달간 집중 학습",
                          "color": "#4CAF50"
                        }
                        """
                )
            }
        )
      )
      public ResponseEntity<ScheduleResponse> update(
          @Parameter(description = "수정할 일정 ID", required = true, example = "1")
          @PathVariable Long id,
          @Valid @RequestBody ScheduleUpdateRequest req) {
        return ResponseEntity.ok(scheduleService.update(id, req));
      }

      @DeleteMapping("/{id}")
      @Operation(
        summary = "일정 삭제",
        description = """
            일정을 영구 삭제합니다.
            
            **주의사항:**
            - 삭제된 일정은 복구할 수 없습니다
            - 포함된 모든 세부 계획(Floor)도 함께 영구 제거됩니다
            - 연관된 완료 기록도 모두 삭제됩니다 (Cascade)
            
            **권한:**
            - JWT 토큰 필수
            - 본인이 생성한 일정만 삭제 가능
            - 팀 일정의 경우 생성자만 삭제 가능
            """
      )
      @ApiResponses({
        @ApiResponse(
            responseCode = "204", 
            description = "삭제 성공 (응답 본문 없음)"
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Unauthorized",
                          "message": "로그인이 필요합니다"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "일정 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Schedule not found",
                          "message": "삭제할 일정이 존재하지 않거나 권한이 없습니다"
                        }
                        """
                )
            )
        )
      })
      public ResponseEntity<Void> delete(
          @Parameter(description = "삭제할 일정 ID", required = true, example = "1")
          @PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
      }
}
