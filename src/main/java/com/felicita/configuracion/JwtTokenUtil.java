package com.felicita.configuracion;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
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

    @Value("${jwt.expiration}")
    private long expiracionJwt;
    
    @Value("${jwt.secret}")
    private String secreto;
    
    private Key obtenerClavesFirma() {
        byte[] bytesClaves = secreto.getBytes();
        return Keys.hmacShaKeyFor(bytesClaves);
    }

    /**
     * Generar token para el usuario
     */
    public String generarToken(UserDetails detallesUsuario) {
        Map<String, Object> claims = new HashMap<>();
        // Agregar roles al token para debugging
        claims.put("roles", detallesUsuario.getAuthorities());
        return crearToken(claims, detallesUsuario.getUsername());
    }
    
    /**
     * Crear token con claims personalizadas
     */
    private String crearToken(Map<String, Object> claims, String sujeto) {
        Date ahora = new Date(System.currentTimeMillis());
        Date fechaExpiracion = new Date(ahora.getTime() + expiracionJwt);
        
        System.out.println("=== CREANDO TOKEN JWT ===");
        System.out.println("Usuario: " + sujeto);
        System.out.println("Fecha creación: " + ahora);
        System.out.println("Fecha expiración: " + fechaExpiracion);
        System.out.println("Tiempo de vida: " + (expiracionJwt / 1000 / 60) + " minutos");
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(sujeto)
                .setIssuedAt(ahora)
                .setExpiration(fechaExpiracion)
                .signWith(obtenerClavesFirma(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Validar token con mejor manejo de errores
     */
    public Boolean validarToken(String token, UserDetails detallesUsuario) {
        try {
            final String nombreUsuario = extraerUsername(token);
            boolean esUsuarioCorrecto = nombreUsuario.equals(detallesUsuario.getUsername());
            boolean noEstaExpirado = !tokenExpirado(token);
            
            System.out.println("=== VALIDANDO TOKEN ===");
            System.out.println("Usuario del token: " + nombreUsuario);
            System.out.println("Usuario esperado: " + detallesUsuario.getUsername());
            System.out.println("Usuario correcto: " + esUsuarioCorrecto);
            System.out.println("Token no expirado: " + noEstaExpirado);
            
            if (!noEstaExpirado) {
                Date expiracion = extraerFechaExpiracion(token);
                System.out.println("Token expiró en: " + expiracion);
                System.out.println("Fecha actual: " + new Date());
            }
            
            return esUsuarioCorrecto && noEstaExpirado;
            
        } catch (ExpiredJwtException e) {
            System.err.println("ERROR: Token JWT expirado - " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.err.println("ERROR: Token JWT malformado - " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.err.println("ERROR: Firma JWT inválida - " + e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            System.err.println("ERROR: Token JWT no soportado - " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: Argumento JWT ilegal - " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("ERROR: Error inesperado al validar token - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Extraer username del token con manejo de errores
     */
    public String extraerUsername(String token) {
        try {
            return extraerClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            System.err.println("Token expirado al extraer username: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error al extraer username del token: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Extraer fecha de expiración
     */
    public Date extraerFechaExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }
    
    /**
     * Verificar si el token ha expirado
     */
    private Boolean tokenExpirado(String token) {
        try {
            final Date expiracion = extraerFechaExpiracion(token);
            return expiracion.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
    
    /**
     * Extraer una claim específica
     */
    public <T> T extraerClaim(String token, Function<Claims, T> resolvedor) {
        final Claims claims = extraerTodasLasClaims(token);
        return resolvedor.apply(claims);
    }
    
    /**
     * Extraer todas las claims con mejor manejo de errores
     */
    private Claims extraerTodasLasClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(obtenerClavesFirma())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.err.println("Token expirado: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.err.println("Token malformado: " + e.getMessage());
            throw e;
        } catch (SignatureException e) {
            System.err.println("Firma inválida: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error al procesar token: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Método de utilidad para debug - obtener información del token
     */
    public void imprimirInfoToken(String token) {
        try {
            Claims claims = extraerTodasLasClaims(token);
            System.out.println("=== INFORMACIÓN DEL TOKEN ===");
            System.out.println("Sujeto: " + claims.getSubject());
            System.out.println("Emitido en: " + claims.getIssuedAt());
            System.out.println("Expira en: " + claims.getExpiration());
            System.out.println("Claims: " + claims);
        } catch (Exception e) {
            System.err.println("Error al obtener información del token: " + e.getMessage());
        }
    }
}