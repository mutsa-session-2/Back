package floorida.example.floorida.Exception.Item;

public class NotEnoughCoinException extends RuntimeException {
    public NotEnoughCoinException(String message) {
        super(message);
    }
}