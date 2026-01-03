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
    public ResponseEntity<?> handleAlreadyOwnedItem(AlreadyOwnedItemException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
    }

    // ===== IllegalStateException (Auth / Permission / Membership) =====
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {
        String msg = e.getMessage();

        if (msg == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "unknown error"));
        }

        // --- Auth ---
        if ("unauthorized".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", msg));
        }

        // --- Team membership ---
        if ("not a team member".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", msg));
        }

        // --- Permission ---
        // TeamService에서 쓰는 메시지 (owner만 가능 등)
        if ("no permission".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", msg));
        }

        // (이전/혼용 대비: leader only 메시지도 지원)
        if ("leader only".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", msg));
        }

        // --- Conflict: already joined ---
        if ("already joined this team".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }

        // --- Not found (혹시 IllegalStateException으로 던져도 404) ---
        if ("team not found".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", msg));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", msg));
    }

    // ===== IllegalArgumentException (Validation / Policy conflict / NotFound) =====
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage();

        if (msg == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "invalid request"));
        }

        // --- Not found ---
        if ("team not found".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", msg));
        }

        // --- Team policy conflicts (409) : owner/member 기준 ---
        if ("owner cannot leave; delete team instead".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }
        if ("owner cannot kick self".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }
        if ("cannot kick owner".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }

        // (이전/혼용 대비: leader 기준 메시지도 지원)
        if ("leader cannot leave; delete team instead".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }
        if ("leader cannot kick self".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }
        if ("cannot kick leader".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", msg));
        }

        // 그 외 검증 오류는 400
        return ResponseEntity.badRequest()
                .body(Map.of("message", msg));
    }
}
