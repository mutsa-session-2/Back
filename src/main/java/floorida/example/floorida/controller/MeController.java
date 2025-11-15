package floorida.example.floorida.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import floorida.example.floorida.dto.OnboardingRequest;
import floorida.example.floorida.entity.User;
import floorida.example.floorida.entity.UserProfile;
import floorida.example.floorida.service.CurrentUserService;
import floorida.example.floorida.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/me")
@Tag(name = "내 정보", description = "로그인한 사용자의 정보/포인트를 조회하는 API")
@SecurityRequirement(name = "bearerAuth")
public class MeController {

    private final CurrentUserService currentUserService;
    private final UserProfileService userProfileService;

    public MeController(CurrentUserService currentUserService, UserProfileService userProfileService) {
        this.currentUserService = currentUserService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 이메일(아이디)을 반환합니다.")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authentication.getName());
    }

    @GetMapping("/points")
    @Operation(
        summary = "내 코인(포인트) 조회",
        description = """
            현재 로그인한 사용자의 보유 코인(포인트)을 조회합니다.

            **코인을 얻는 상황**
            - 퀘스트(Floor) 하나 체크할 때마다 **10코인**
            - 처음 회원가입하고 로그인 했을 때 **50코인**

            **권한**
            - JWT 토큰 필수
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Integer.class),
                examples = @ExampleObject(name = "보유 코인 예시", value = "120")
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<Integer> getMyPoints() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        int points = userProfileService.getPoints(user.getUserId());
        return ResponseEntity.ok(points);
    }

    @GetMapping("/profile")
    @Operation(
        summary = "내 프로필 조회",
        description = """
            로그인한 사용자의 UserProfile 정보를 조회합니다.

            - points: 현재 코인
            - personalLevel: 개인 층수
            - planningTendency / dailyStudyHours: 온보딩에서 사용할 계획/공부 시간 성향
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfile.class),
                examples = @ExampleObject(
                    name = "프로필 예시",
                    value = """
                        {
                          "userId": 1,
                          "points": 120,
                          "personalLevel": 3,
                          "planningTendency": "PLANS_AND_EXECUTES",
                          "dailyStudyHours": "HOURS_1_3"
                        }
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
    public ResponseEntity<UserProfile> getMyProfile() {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        UserProfile profile = userProfileService.getProfile(user.getUserId());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/onboarding")
    @Operation(
        summary = "온보딩 정보 저장",
        description = """
            회원가입 이후 온보딩 화면에서 선택한 계획 성향 / 하루 공부 시간을 저장합니다.

            - planningTendency:
              - PROCRASTINATES     (할 일을 최대한 미룬다)
              - PLANS_ONLY         (계획은 세우는데 실천하지 못한다)
              - PLANS_AND_EXECUTES (꼼꼼하게 계획을 세우고 이행한다)

            - dailyStudyHours:
              - HOURS_0_1
              - HOURS_1_3
              - HOURS_3_6
              - HOURS_6_10
              - HOURS_10_PLUS

            둘 다 선택하지 않아도 되고, 나중에 다시 호출해도 됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "저장 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfile.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<UserProfile> saveOnboarding(@RequestBody OnboardingRequest request) {
        User user = currentUserService.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("Unauthenticated"));

        // 프로필이 아직 없을 수도 있으니, 먼저 기본 프로필(가입 보너스 포함)을 보장
        userProfileService.ensureSignupBonusOnFirstLogin(user);

        UserProfile updated = userProfileService.updateOnboarding(
                user.getUserId(),
                request.getPlanningTendency(),
                request.getDailyStudyHours()
        );

        return ResponseEntity.ok(updated);
    }
}
