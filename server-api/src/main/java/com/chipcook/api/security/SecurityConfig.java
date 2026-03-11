package com.chipcook.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(HttpMethod.POST, "/api/usuario/registrar").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuario/login").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/usuario/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/usuario/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuario/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/estoque/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/estoque/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/estoque/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/pedidos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/funcionario/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tenant/current").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/produtos/**").permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
