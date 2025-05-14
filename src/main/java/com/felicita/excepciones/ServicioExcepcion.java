package com.felicita.excepciones;

public class ServicioExcepcion extends RuntimeException {
    public ServicioExcepcion(String mensaje) {
        super(mensaje);
    }
}