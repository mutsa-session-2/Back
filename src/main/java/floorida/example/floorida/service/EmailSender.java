package floorida.example.floorida.service;

public interface EmailSender {
    void send(String to, String subject, String text);
}
