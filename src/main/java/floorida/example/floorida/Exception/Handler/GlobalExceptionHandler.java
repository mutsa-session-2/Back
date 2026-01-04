package floorida.example.floorida.Exception.Handler;

import floorida.example.floorida.Exception.Item.AlreadyOwnedItemException;
import floorida.example.floorida.Exception.Item.NotEnoughCoinException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
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

    // ===== Validation (@Valid) =====
    // DTO에 @NotNull, @NotBlank 등 걸어둔 경우 여기로 들어옴 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "validation failed");

        // 어떤 필드가 왜 실패했는지 같이 내려주면 프론트/테스트가 훨씬 편해짐
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> {
            // 같은 필드 에러가 여러개면 마지막이 덮어쓰게 됨(원하면 리스트로 바꿀 수 있음)
            errors.put(fe.getField(), fe.getDefaultMessage());
        });

        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // ===== EntityNotFoundException =====
    // JPA findById().orElseThrow(EntityNotFoundException) 같은 케이스는 404로 처리
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) msg = "not found";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", msg));
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

        // --- TeamFloor 권한(배정자만 가능 등) ---
        // 너 코드에서: "Only assignees can complete/cancel this task."
        if (msg.toLowerCase().contains("only assignees")) {
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

        // --- TeamFloor validation (기간 밖 dueDate 등) ---
        // 너 서비스에서: "dueDate out of team period"
        if ("dueDate out of team period".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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

    // ===== Fallback (500) =====
    // 위에서 못 잡은 예외는 여기로. 응답 포맷 통일 + 디버깅용
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) msg = "internal server error";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", msg));
    }
}
