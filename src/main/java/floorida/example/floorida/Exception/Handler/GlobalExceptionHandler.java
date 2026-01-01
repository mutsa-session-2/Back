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

    @ExceptionHandler(NotEnoughCoinException.class)
    public ResponseEntity<?> handleNotEnoughCoin(NotEnoughCoinException e) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(AlreadyOwnedItemException.class)
    public ResponseEntity<?> handleAlreadyOwned(AlreadyOwnedItemException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // ⭐ 409
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", e.getMessage()));
    }

}