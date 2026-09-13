package kr.zipimoney.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.zipimoney.global.response.ApiResponse;
import kr.zipimoney.global.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/kakao").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        // Public API endpoints
                        .requestMatchers(HttpMethod.GET, "/api/region").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/geocode").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/deals").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/complex").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/calculate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/share").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cron/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/og/**").permitAll()
                        // Short URL
                        .requestMatchers("/s/**").permitAll()
                        // Swagger / API docs
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        // Static resources
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
                        // Authenticated endpoints
                        .requestMatchers("/api/auth/me", "/api/auth/withdraw").authenticated()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/consent/**").authenticated()
                        .requestMatchers("/api/history").authenticated()
                        // Allow all other requests (SPA static resources)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ApiResponse<?> apiResponse = ApiResponse.builder()
                                    .error(new ApiResponse.Error("UNAUTHORIZED", "Authentication required"))
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                        })
                );

        return http.build();
    }

    @SuppressWarnings("deprecation")
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
