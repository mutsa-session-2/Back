package floorida.example.floorida.team.dto;

import jakarta.validation.constraints.NotBlank;

public class TeamDeleteRequest {

    @NotBlank
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
