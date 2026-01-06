package floorida.example.floorida.Exception.Handler;

import floorida.example.floorida.Exception.Item.AlreadyOwnedItemException;
import floorida.example.floorida.Exception.Item.NotEnoughCoinException;
import floorida.example.floorida.Exception.Item.TeamAccessDeniedException;
import floorida.example.floorida.Exception.Item.TeamNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
    // DTO @NotBlank, @NotNull 등 검증 실패 시 400 반환
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        String msg = "invalid request";

        // 첫 번째 에러 메시지 우선 반환(너무 길어지는 거 방지)
        if (e.getBindingResult() != null && e.getBindingResult().getFieldError() != null) {
            String field = e.getBindingResult().getFieldError().getField();
            String fieldMsg = e.getBindingResult().getFieldError().getDefaultMessage();
            msg = field + ": " + fieldMsg;
        }

        return ResponseEntity.badRequest()
                .body(Map.of("message", msg));
    }

    // ===== JPA NotFound =====
    // EntityNotFoundException을 그대로 두면 500으로 떨어질 수 있어서 404로 매핑
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException e) {
        String msg = e.getMessage();
        if (msg == null) msg = "not found";
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
        if ("unauthorized".equalsIgnoreCase(msg) || "unauthenticated".equalsIgnoreCase(msg)) {
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
        if ("user not found".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", msg));
        }
        if ("schedule not found".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", msg));
        }
        if ("floor not found".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", msg));
        }
        if ("invalid join code".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", msg));
        }

        // --- Permission ---
        if (msg.toLowerCase().startsWith("not authorized")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", msg));
        }

        // --- Password (팀 삭제 재인증) ---
        if ("invalid password".equalsIgnoreCase(msg)) {
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

    // ===== DB Integrity / FK constraint =====
    // FK 제약 등으로 삭제/수정이 실패하는 경우 409로 매핑
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "conflict"));
    }

    // ===== Path Variable Type Mismatch =====
    // PathVariable 타입 불일치 (예: /api/schedules/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = "invalid parameter: " + e.getName();
        return ResponseEntity.badRequest()
                .body(Map.of("message", msg));
    }

    // ===== No Handler Found =====
    // 존재하지 않는 경로 요청 시 404 반환
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "not found"));
    }

    // ===== Static Resource Not Found =====
    // 정적 리소스 핸들러가 리소스를 못 찾는 경우도 404로 반환
    // (Global catch-all이 500으로 바꾸지 않도록 분리)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "not found"));
    }

    // ===== Method Not Supported =====
    // 지원하지 않는 HTTP 메서드 (예: POST /api/schedules/{id})
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Map.of("message", "method not allowed"));
    }

    // ===== Fallback =====
    // 예상 못한 예외는 500으로 떨어지되, message는 과하게 노출하지 않음(보안/UX)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknown(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "internal server error"));
    }

    //캐릭터 조회 관련 exception handler
    @ExceptionHandler(TeamAccessDeniedException.class)
    public ResponseEntity<String> handleTeamAccessDenied(TeamAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<String> handleTeamNotFound(TeamNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
