package floorida.example.floorida.Exception.Handler;

import floorida.example.floorida.Exception.Item.AlreadyOwnedItemException;
import floorida.example.floorida.Exception.Item.NotEnoughCoinException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== Item =====
    @ExceptionHandler(NotEnoughCoinException.class)
    public ResponseEntity<?> handleNotEnoughCoin(NotEnoughCoinException e) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(AlreadyOwnedItemException.class)
    public ResponseEntity<?> handleAlreadyOwned(AlreadyOwnedItemException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
    }

    // ===== Team/Auth etc (현재는 IllegalStateException 메시지 분기) =====
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        String msg = e.getMessage();

        if (msg != null && "unauthorized".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", msg));
        }
        if (msg != null && "already joined this team".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }
        if (msg != null && "not a team member".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", msg));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", msg));
    }

    // ===== Common =====
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
    }

}
