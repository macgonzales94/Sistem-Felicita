package com.felicita.excepciones;

public class UsuarioExcepcion extends RuntimeException {
    public UsuarioExcepcion(String mensaje) {
        super(mensaje);
    }
}