package floorida.example.floorida.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 운영 환경(prod)에서만 배포 완료 알림을 Discord로 전송합니다.
 * 로컬 환경에서는 이 컴포넌트가 활성화되지 않습니다.
 */
@Component
@Profile("prod")
public class DeployNotifier implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(DeployNotifier.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String webhookUrl = System.getenv("DISCORD_WEBHOOK_URL");
        
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("DISCORD_WEBHOOK_URL 환경변수가 설정되지 않아 배포 알림을 전송하지 않습니다.");
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String message = String.format(
                    "✅ **Floorida 서버 배포 완료!**\n" +
                    "🕐 시간: %s\n" +
                    "🚀 서버가 정상적으로 시작되었습니다.",
                    timestamp
            );
            
            Map<String, String> body = Map.of("content", message);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            
            restTemplate.postForEntity(webhookUrl, request, String.class);
            log.info("Discord 배포 알림 전송 완료");
            
        } catch (Exception e) {
            log.error("Discord 배포 알림 전송 실패: {}", e.getMessage());
        }
    }
}
