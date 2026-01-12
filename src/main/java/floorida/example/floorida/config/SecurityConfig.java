package floorida.example.floorida.config;

import floorida.example.floorida.config.jwt.SetCustomUserDetailsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

import floorida.example.floorida.config.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SetCustomUserDetailsFilter setCustomUserDetailsFilter;
    private final BotDetectionFilter botDetectionFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          SetCustomUserDetailsFilter setCustomUserDetailsFilter,
                          BotDetectionFilter botDetectionFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.setCustomUserDetailsFilter = setCustomUserDetailsFilter;
        this.botDetectionFilter = botDetectionFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // Enable CORS with our CorsConfigurationSource bean
            .cors(cors -> {})
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (root, error, static resources)
                .requestMatchers("/", "/index.html", "/error", "/favicon.ico",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/static/**").permitAll()
                // Health check pages
                .requestMatchers("/health/**", "/api/health/**").permitAll()
                // Swagger/OpenAPI docs are NOT public (secured via SwaggerBasicAuthConfig or JWT)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").authenticated()
                // Allow CORS preflight requests universally
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Everything else requires auth
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(botDetectionFilter, JwtAuthenticationFilter.class)
            .addFilterAfter(setCustomUserDetailsFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow ALL origins - fully open CORS
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
