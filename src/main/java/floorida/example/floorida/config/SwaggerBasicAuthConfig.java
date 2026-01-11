package floorida.example.floorida.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
public class SwaggerBasicAuthConfig {

    /**
     * Standard protection for API docs.
     *
     * This is real access control (unlike JS copy-blocking). Configure credentials via:
     * - app.swagger.basic.username
     * - app.swagger.basic.password
     */

    @Bean
    public UserDetailsService swaggerUserDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.swagger.basic.username:${APP_SWAGGER_BASIC_USERNAME:swagger}}") String username,
            @Value("${app.swagger.basic.password:${APP_SWAGGER_BASIC_PASSWORD:change-me}}") String password
    ) {
        UserDetails user = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("SWAGGER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    public SecurityFilterChain swaggerSecurityFilterChain(
            HttpSecurity http,
            @Value("${app.swagger.basic.realmSuffix:${APP_SWAGGER_BASIC_REALM_SUFFIX:}}") String realmSuffix
    ) throws Exception {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        // NOTE: Browsers cache BasicAuth per (origin + realm). Change this value to force re-prompt.
        // You can override via env: APP_SWAGGER_BASIC_REALM_SUFFIX=any-random-string
        String realm = "FlooridaSwagger" + realmSuffix;
        entryPoint.setRealmName(realm);
        entryPoint.afterPropertiesSet();

        http
                .securityMatcher("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Avoid caching Swagger resources in browsers/proxies.
            .headers(headers -> headers.addHeaderWriter(new StaticHeadersWriter(
                HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("SWAGGER"))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
