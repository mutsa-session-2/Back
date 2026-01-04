package floorida.example.floorida.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TeamFloorUpdateRequest {

    @NotBlank
    private String title;

    // 마감일 null 허용(마감 없는 할 일 가능)로 둘 거면 @NotNull 안 붙임
    private LocalDate dueDate;

    @NotNull
    public String getTitle() {
        return title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
