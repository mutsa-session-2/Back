package floorida.example.floorida.team.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TeamFloorStatusId implements Serializable {

    @Column(name = "team_floor_id")
    private Long teamFloorId;

    @Column(name = "user_id")
    private Long userId;
}
