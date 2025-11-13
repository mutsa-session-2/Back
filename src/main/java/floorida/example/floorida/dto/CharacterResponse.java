package floorida.example.floorida.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "캐릭터 응답")
public record CharacterResponse(
    @Schema(description = "캐릭터 ID", example = "1")
    Long characterId,
    
    @Schema(description = "캐릭터 이미지 URL", example = "https://bucket-gc5ukj.s3.us-east-1.amazonaws.com/캐릭터.png")
    String imageUrl
) {}
