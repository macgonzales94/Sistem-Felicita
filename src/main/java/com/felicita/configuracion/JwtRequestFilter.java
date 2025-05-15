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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
            
        // Primero verificamos si hay un token en las cookies
        String jwtToken = null;
        String username = null;
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    try {
                        username = jwtTokenUtil.extraerUsername(jwtToken);
                    } catch (Exception e) {
                        logger.warn("Token JWT en cookie inválido");
                    }
                    break;
                }
            }
        }
        
        // Si no hay un token en las cookies, verificamos en el header Authorization
        if (username == null) {
            final String requestTokenHeader = request.getHeader("Authorization");
            
            // El token JWT está en el formato "Bearer token"
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                try {
                    username = jwtTokenUtil.extraerUsername(jwtToken);
                } catch (Exception e) {
                    logger.warn("Token JWT inválido en el header");
                }
            } else {
                logger.debug("JWT no comienza con la cadena Bearer o no está presente en el header");
            }
        }
        
        // Validar el token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.usuarioDetallesServicio.loadUserByUsername(username);
            
            if (jwtTokenUtil.validarToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                        
                usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}