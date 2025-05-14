package com.felicita.configuracion;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    // Tiempo de expiración del token: 24 horas
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.secret}")
    private String secret;
    
    private Key getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generar token para el usuario
    public String generarToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return crearToken(claims, userDetails.getUsername());
    }
    
    private String crearToken(Map<String, Object> claims, String subject) {
        Date ahora = new Date(System.currentTimeMillis());
        Date fechaExpiracion = new Date(ahora.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(ahora)
                .setExpiration(fechaExpiracion)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Validar token
    public Boolean validarToken(String token, UserDetails userDetails) {
        final String username = extraerUsername(token);
        return (username.equals(userDetails.getUsername()) && !tokenExpirado(token));
    }
    
    // Extraer username del token
    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }
    
    // Extraer fecha de expiración
    public Date extraerFechaExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }
    
    // Verificar si el token ha expirado
    private Boolean tokenExpirado(String token) {
        final Date expiracion = extraerFechaExpiracion(token);
        return expiracion.before(new Date());
    }
    
    // Extraer una claim específica
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodasLasClaims(token);
        return claimsResolver.apply(claims);
    }
    
    // Extraer todas las claims
    private Claims extraerTodasLasClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}