package com.serveflow.Config;

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
import com.serveflow.Config.Jwt.JwtFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsRaw;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOriginsRaw.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/forgot-password").permitAll()
                        .requestMatchers("/auth/reset-password").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Cadastro de usuários: apenas ADMIN/ROOT
                        .requestMatchers(HttpMethod.POST, "/auth/register").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers(HttpMethod.POST, "/users").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("ADMIN", "ROOT")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ROOT")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("ADMIN", "ROOT")

                        // Troca da própria senha: qualquer autenticado
                        .requestMatchers("/auth/change-password").authenticated()

                        // Módulos por perfil (RBAC)
                        .requestMatchers("/menu/**").hasAnyRole("GARCON", "ADMIN", "ROOT", "COZINHEIRO")
                        .requestMatchers("/kds/**").hasAnyRole("COZINHEIRO", "ADMIN", "ROOT")
                        .requestMatchers("/cashier/**").hasAnyRole("CAIXA", "ADMIN", "ROOT")
                        .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "ROOT")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
