package floorida.example.floorida.Exception.Item;

public class AlreadyOwnedItemException extends RuntimeException {
    public AlreadyOwnedItemException(String message) {
        super(message);
    }
}