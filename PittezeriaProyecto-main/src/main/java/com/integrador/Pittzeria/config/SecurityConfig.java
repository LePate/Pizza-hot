package com.integrador.Pittzeria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Herramienta obligatoria para encriptar contraseñas
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Permitir archivos estáticos (CSS, JS, Imágenes) sin iniciar sesión
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()

                        // Restringir rutas según los roles de tu BD (RF01)
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/cajero/**", "/venta/**").hasAnyRole("ADMINISTRADOR", "CAJERO")

                        // Cualquier otra petición requiere estar logueado
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Nuestra vista personalizada (login.html)
                        .defaultSuccessUrl("/index", true) // A dónde va tras loguearse con éxito
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}