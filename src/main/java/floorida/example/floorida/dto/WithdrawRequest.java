package floorida.example.floorida.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawRequest {

    @NotBlank
    private String password;

    @AssertTrue(message = "You must confirm withdrawal")
    private boolean confirmed;
}
