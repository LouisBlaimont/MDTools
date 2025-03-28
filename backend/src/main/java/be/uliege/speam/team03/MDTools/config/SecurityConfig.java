package be.uliege.speam.team03.MDTools.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import be.uliege.speam.team03.MDTools.services.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${app.allowed-origin}")
    public String frontendUrl;

    private final OAuth2UserService oAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**").permitAll() // Allow health check
                        .requestMatchers(HttpMethod.OPTIONS).permitAll() // Allow preflight requests
                        .anyRequest().authenticated() // Require authentication for all requests
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            // Redirect to frontend after successful login
                            response.sendRedirect(frontendUrl + "/?login=success");
                        })
                        .failureHandler((request, response, authentication) -> {
                            // Redirect to frontend after failed login
                            response.sendRedirect(frontendUrl + "/?login=failed");
                        })
                        )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Redirect to frontend after successful logout, seems like it is not using the
                            // default cors
                            response.setStatus(204);
                            response.setHeader("Access-Control-Allow-Origin", frontendUrl);
                            response.setHeader("Access-Control-Allow-Methods", "GET, PATCH, POST, OPTIONS");
                            response.setHeader("Access-Control-Allow-Headers", "*");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                        }))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.info(authException.getMessage() + ", " + authException.getCause());
                            response.setStatus(401);
                            response.setHeader("Access-Control-Allow-Origin", frontendUrl);
                            response.setHeader("Access-Control-Allow-Methods", "GET, PATCH, POST, OPTIONS");
                            response.setHeader("Access-Control-Allow-Headers", "*");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.info(accessDeniedException.getMessage() + ", " + accessDeniedException.getCause());
                            response.setStatus(403);
                            response.setHeader("Access-Control-Allow-Origin", frontendUrl);
                            response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
                            response.setHeader("Access-Control-Allow-Headers", "*");
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                        }));

        return http.build();
    }
}
