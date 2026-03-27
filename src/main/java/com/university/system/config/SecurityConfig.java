package com.university.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/enrollments/**").hasAnyRole("TEACHER", "ADMIN", "STUDENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    var roles = authentication.getAuthorities().stream()
                                                .map(r -> r.getAuthority())
                                                .toList();
                    
                    System.out.println("DEBUG: Success login. Roles: " + roles);

                    if (roles.contains("ROLE_ADMIN")) {
                        response.sendRedirect("/admin/dashboard");
                    } else if (roles.contains("ROLE_TEACHER")) {
                        response.sendRedirect("/teacher/dashboard");
                    } else if (roles.contains("ROLE_STUDENT")) {
                        response.sendRedirect("/student/dashboard");
                    } else {
                        response.sendRedirect("/courses");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}