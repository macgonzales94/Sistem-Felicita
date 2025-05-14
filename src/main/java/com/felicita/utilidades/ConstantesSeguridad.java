package com.felicita.utilidades;

public class ConstantesSeguridad {
    // Prefijo para roles
    public static final String ROLE_PREFIX = "ROLE_";
    
    // Nombres de roles
    public static final String ROLE_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String ROLE_PROADMIN = "PROADMIN";
    public static final String ROLE_CLIENTE = "CLIENTE";
    
    // Constantes para JWT
    public static final long JWT_EXPIRATION_TIME = 86400000; // 1 día en milisegundos
    public static final String JWT_SECRET = "ClaveSecretaParaGenerarTokensJWTEnNuestraAplicacionFelicita";
    
    // Constantes para cabeceras HTTP
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
}