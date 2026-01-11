package floorida.example.floorida.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FloorCompleteResponse {
    private Long floorId;
    private boolean completed;
    private int coinsAwarded;
    private int currentPoints;
    private Instant completedAt;
}
