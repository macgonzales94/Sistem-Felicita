package com.felicita.configuracion;

import com.felicita.servicios.impl.UsuarioDetallesServicioImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioDetallesServicioImpl usuarioDetallesServicio;
    
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta, FilterChain filtroChain)
            throws ServletException, IOException {
            
        String tokenJwt = null;
        String nombreUsuario = null;
        
        // PASO 1: Buscar token en cookies primero
        Cookie[] cookies = solicitud.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    tokenJwt = cookie.getValue();
                    logger.debug("Token encontrado en cookie: " + (tokenJwt != null ? "Sí" : "No"));
                    try {
                        if (tokenJwt != null && !tokenJwt.trim().isEmpty()) {
                            nombreUsuario = jwtTokenUtil.extraerUsername(tokenJwt);
                            logger.debug("Usuario extraído del token: " + nombreUsuario);
                        }
                    } catch (Exception e) {
                        logger.warn("Token JWT en cookie inválido: " + e.getMessage());
                        // Limpiar token inválido
                        tokenJwt = null;
                        nombreUsuario = null;
                    }
                    break;
                }
            }
        }
        
        // PASO 2: Si no hay token en cookies, verificar header Authorization
        if (nombreUsuario == null) {
            final String cabeceraToken = solicitud.getHeader("Authorization");
            
            if (cabeceraToken != null && cabeceraToken.startsWith("Bearer ")) {
                tokenJwt = cabeceraToken.substring(7);
                try {
                    if (!tokenJwt.trim().isEmpty()) {
                        nombreUsuario = jwtTokenUtil.extraerUsername(tokenJwt);
                        logger.debug("Usuario extraído del header: " + nombreUsuario);
                    }
                } catch (Exception e) {
                    logger.warn("Token JWT en header inválido: " + e.getMessage());
                    tokenJwt = null;
                    nombreUsuario = null;
                }
            }
        }
        
        // PASO 3: Validar token y establecer autenticación
        if (nombreUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails detallesUsuario = this.usuarioDetallesServicio.loadUserByUsername(nombreUsuario);
                
                // Validar que el token sea válido
                if (jwtTokenUtil.validarToken(tokenJwt, detallesUsuario)) {
                    UsernamePasswordAuthenticationToken tokenAutenticacion = 
                        new UsernamePasswordAuthenticationToken(
                            detallesUsuario, null, detallesUsuario.getAuthorities());
                            
                    tokenAutenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
                    SecurityContextHolder.getContext().setAuthentication(tokenAutenticacion);
                    
                    logger.debug("Autenticación establecida para usuario: " + nombreUsuario + 
                               " con roles: " + detallesUsuario.getAuthorities());
                } else {
                    logger.warn("Token JWT no válido para usuario: " + nombreUsuario);
                }
            } catch (Exception e) {
                logger.error("Error al procesar autenticación JWT: " + e.getMessage());
            }
        }
        
        filtroChain.doFilter(solicitud, respuesta);
    }
}