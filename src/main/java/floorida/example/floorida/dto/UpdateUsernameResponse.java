package floorida.example.floorida.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateUsernameResponse {
    private String username;

    public String getUsername() {
        if (this.username == null) return null;
        return this.username + "\u200B";
    }
}
