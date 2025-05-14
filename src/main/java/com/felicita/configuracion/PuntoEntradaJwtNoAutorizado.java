package com.felicita.configuracion;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PuntoEntradaJwtNoAutorizado implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Para solicitudes AJAX o API
        if (request.getHeader("X-Requested-With") != null 
            || request.getRequestURI().startsWith("/api/")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado");
        } else {
            // Redirigir a la página de login para solicitudes normales
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}