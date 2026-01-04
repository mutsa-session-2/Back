package floorida.example.floorida.teamBoard.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamBoardCommentCreateRequest {

    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 500, message = "댓글은 최대 500자까지 작성할 수 있습니다.")
    private String content;
}