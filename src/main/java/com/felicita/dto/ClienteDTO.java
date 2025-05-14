package com.felicita.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {
    private Long id;
    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String codigoPostal;
    private String preferencias;
    private long cantidadCitas;
}