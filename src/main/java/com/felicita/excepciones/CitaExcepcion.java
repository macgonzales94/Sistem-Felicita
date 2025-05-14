package com.felicita.excepciones;

public class CitaExcepcion extends RuntimeException {
    public CitaExcepcion(String mensaje) {
        super(mensaje);
    }
}