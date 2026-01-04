package floorida.example.floorida.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.email.enabled", havingValue = "false")
public class NoopEmailSender implements EmailSender {
    @Override
    public void send(String to, String subject, String text) {
        // no-op
    }
}
