package floorida.example.floorida.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class BotDetectionFilter extends OncePerRequestFilter {

    // 차단할 봇 User-Agent 키워드 목록
    private static final List<String> BLOCKED_USER_AGENTS = Arrays.asList(
            "curl", "wget", "python", "scrapy", "httpclient", "postman", "selenium", "puppeteer"
            // 필요에 따라 "googlebot", "bingbot" 등을 추가하거나 제외하세요.
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userAgent = request.getHeader("User-Agent");

        // 1. User-Agent가 아예 없거나 예외적인 경우
        if (userAgent == null || userAgent.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied: Missing User-Agent");
            return;
        }

        // 2. 봇 키워드가 포함되어 있는지 확인
        if (isSuspiciousBot(userAgent)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Access Denied: Bot detected");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSuspiciousBot(String userAgent) {
        String lowerAgent = userAgent.toLowerCase();
        return BLOCKED_USER_AGENTS.stream().anyMatch(lowerAgent::contains);
    }
}
