package com.felicita.excepciones;

public class EstablecimientoExcepcion extends RuntimeException {
    public EstablecimientoExcepcion(String mensaje) {
        super(mensaje);
    }
}