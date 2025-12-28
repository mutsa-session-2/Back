package floorida.example.floorida.config.jwt;

import java.io.IOException;

import floorida.example.floorida.Item.UserDetails.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import floorida.example.floorida.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 기존 JwtAuthenticationFilter 뒤에서 실행되어
 * SecurityContext의 principal을 CustomUserDetails로 변환
 */
@Component
public class SetCustomUserDetailsFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SetCustomUserDetailsFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증이 있고, principal이 String(email)인 경우만 처리
        if (auth != null && auth.getPrincipal() instanceof String email) {
            userRepository.findByEmail(email).ifPresent(user -> {
                CustomUserDetails userDetails = new CustomUserDetails(
                        user.getUserId(),
                        user.getEmail(),
                        user.getPasswordHash()
                );

                // SecurityContext에 새 Authentication으로 교체
                SecurityContextHolder.getContext().setAuthentication(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                userDetails,
                                auth.getCredentials(),
                                auth.getAuthorities()
                        )
                );
            });
        }

        filterChain.doFilter(request, response);
    }
}
