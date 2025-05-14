package com.felicita.configuracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    @Autowired
    private ManejadorAccesoDenegado manejadorAccesoDenegado;
    
    @Autowired
    private PuntoEntradaJwtNoAutorizado puntoEntradaJwtNoAutorizado;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler(manejadorAccesoDenegado)
                .authenticationEntryPoint(puntoEntradaJwtNoAutorizado)
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authorize -> authorize
                // Recursos estáticos públicos
                .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                // Páginas públicas
                .requestMatchers("/", "/index", "/registro", "/login", "/acerca", "/contacto").permitAll()
                // API pública para el login y registro
                .requestMatchers("/api/auth/**").permitAll()
                // Rutas para administradores
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                // Rutas para ProAdmin
                .requestMatchers("/proadmin/**").hasRole("PROADMIN")
                // Rutas para clientes
                .requestMatchers("/cliente/**").hasRole("CLIENTE")
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            );
            
            
            
        // Agregar filtro JWT
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

