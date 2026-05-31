package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    @Value("${actuator.username:actuator}")
    private String username;

    @Value("${actuator.password:actuator}")
    private String password;

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(
            HttpSecurity http,
            PasswordEncoder passwordEncoder) throws Exception {

        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to("health")).permitAll()
                .anyRequest().hasRole("ACTUATOR")
            )
            .httpBasic(basic -> {})
            .csrf(csrf -> csrf.disable())
            .userDetailsService(actuatorUserDetailsService(passwordEncoder));

        return http.build();
    }

    private UserDetailsService actuatorUserDetailsService(PasswordEncoder passwordEncoder) {
        var actuatorUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles("ACTUATOR")
                .build();
        return new InMemoryUserDetailsManager(actuatorUser);
    }
}