package floorida.example.floorida.controller;

import floorida.example.floorida.dto.CharacterResponse;
import floorida.example.floorida.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@Tag(name = "캐릭터", description = "사용자 캐릭터 조회 API")
@SecurityRequirement(name = "Bearer Authentication")
public class CharacterController {
    
    private final CharacterService characterService;

    @GetMapping("/me")
    @Operation(
        summary = "내 캐릭터 조회",
        description = """
            현재 로그인한 사용자의 캐릭터 이미지를 조회합니다.
            
            **특징:**
            - 회원가입 시 자동으로 기본 캐릭터 할당
            - AWS S3에 저장된 이미지 URL 반환
            
            **권한:**
            - JWT 토큰 필수
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CharacterResponse.class),
                examples = @ExampleObject(
                    name = "캐릭터 조회 성공",
                    value = """
                        {
                          "characterId": 1,
                          "imageUrl": "https://bucket-gc5ukj.s3.us-east-1.amazonaws.com/캐릭터.png"
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
            description = "캐릭터를 찾을 수 없음",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<CharacterResponse> getMyCharacter() {
        return ResponseEntity.ok(characterService.getMyCharacter());
    }
}
