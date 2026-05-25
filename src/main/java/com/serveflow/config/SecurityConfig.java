package com.serveflow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsRaw;

    @Value("${app.swagger.enabled:true}")
    private boolean swaggerEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOriginsRaw.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {

                auth.requestMatchers(
                        "/auth/login", "/auth/refresh",
                        "/auth/identify", "/auth/forgot-password",
                        "/auth/verify-reset-token", "/auth/reset-password"
                ).permitAll();

                auth.requestMatchers("/ws/**").authenticated();

                if (swaggerEnabled) {
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
                } else {
                    auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN");
                }

                auth.requestMatchers(HttpMethod.GET,  "/uploads/**").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/uploads/image")
                        .hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO");

                auth.requestMatchers(HttpMethod.GET,   "/products", "/products/**").authenticated();
                auth.requestMatchers(HttpMethod.POST,  "/products", "/products/batch")
                        .hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO");
                auth.requestMatchers(HttpMethod.PUT,   "/products/**")
                        .hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO");
                auth.requestMatchers(HttpMethod.PATCH, "/products/**")
                        .hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO");
                auth.requestMatchers(HttpMethod.DELETE,"/products/**")
                        .hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO");

                auth.requestMatchers(HttpMethod.POST,  "/users").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.PUT,   "/users/**").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.PATCH, "/users/*/password").authenticated();
                auth.requestMatchers(HttpMethod.PATCH, "/users/*/reset-password")
                        .hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.PATCH, "/users/*/job-position")
                        .hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.DELETE,"/users/**").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.GET,   "/users/**").hasAnyRole("ADMIN", "GERENTE");

                auth.requestMatchers(HttpMethod.GET,   "/stock/**").authenticated();
                auth.requestMatchers(HttpMethod.POST,  "/stock/**").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.PUT,   "/stock/**").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.PATCH, "/stock/**").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.DELETE,"/stock/**").hasAnyRole("ADMIN", "GERENTE");

                auth.requestMatchers(HttpMethod.GET,   "/menus/**").authenticated();
                auth.requestMatchers(HttpMethod.POST,  "/menus").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.PATCH, "/menus/*/items/*/availability")
                        .hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO");
                auth.requestMatchers(HttpMethod.PATCH, "/menus/**").hasAnyRole("ADMIN", "GERENTE");
                auth.requestMatchers(HttpMethod.DELETE,"/menus/**").hasAnyRole("ADMIN", "GERENTE");

                auth.requestMatchers(HttpMethod.GET,   "/orders/**").authenticated();
                auth.requestMatchers(HttpMethod.POST,  "/orders/**")
                        .hasAnyRole("ADMIN", "GERENTE", "GARCON");
                auth.requestMatchers(HttpMethod.PATCH, "/orders/**")
                        .hasAnyRole("ADMIN", "GERENTE", "GARCON", "COZINHEIRO", "CAIXA");

                auth.requestMatchers(HttpMethod.GET,   "/kds/**")
                        .hasAnyRole("ADMIN", "GERENTE", "GARCON", "COZINHEIRO");
                auth.requestMatchers(HttpMethod.PATCH, "/kds/**")
                        .hasAnyRole("ADMIN", "GERENTE", "COZINHEIRO");

                auth.requestMatchers("/cashier/**").hasAnyRole("ADMIN", "GERENTE", "CAIXA");
                auth.requestMatchers("/financial/**").hasAnyRole("ADMIN", "GERENTE", "CAIXA");
                auth.requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "GERENTE");

                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
